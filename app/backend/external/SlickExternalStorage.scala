package backend.external

import java.time.LocalDateTime

import backend.recon.{Album, Artist, Reconcilable}
import backend.storage.{AlwaysFresh, DatedFreshness, DbProvider, Freshness, SlickSingleKeyColumnStorageTemplateFromConf}
import com.google.inject.{Inject, Singleton}
import io.lemonlabs.uri.Url
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.JdbcType

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps
import common.rich.func.kats.ToMoreFoldableOps._
import common.rich.func.kats.ToMoreMonadErrorOps._

import common.rich.RichT._
import common.storage.{ColumnMappersSpecVer, StringSerializable}

// TODO replace with composition
private abstract class SlickExternalStorage[R <: Reconcilable](
    ec: ExecutionContext,
    dbP: DbProvider,
) extends SlickSingleKeyColumnStorageTemplateFromConf[R, (MarkedLinks[R], Freshness)](ec, dbP)
    with ExternalStorage[R] {
  private case class InvalidEntry(entry: String) extends Exception
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  private implicit def markedLinkStringSerializable: StringSerializable[MarkedLink[R]] =
    // FIXME some URLs include ";". We solve this by using "-;-" in those rare cases, but one should find a less hacky hack.
    new StringSerializable[MarkedLink[R]] {
      private val SplitChar = ";"
      private val SplitCharBackup = "-;-"
      override def separator = ";;"
      override def parse(s: String): MarkedLink[R] = {
        val split = s
          .split(if (s.contains(SplitCharBackup)) SplitCharBackup else SplitChar)
          .ifNot(_.length == 4)
          .thenThrow(InvalidEntry(s))
        val url = Url.parse(split(1))
        val mark = {
          val markText = split(3)
          LinkMark.withNameOption(markText).getOrElse(LinkMark.Text.read(markText))
        }
        MarkedLink[R](
          link = Url.parse(split(2)),
          host = Host.withUrl(url).getOrElse(Host(name = split(0), url = url)),
          mark = mark,
        )
      }
      override def stringify(e: MarkedLink[R]): String = {
        val encodedLink =
          Vector(e.host.name, e.host.url.toStringPunycode, e.link.toStringPunycode, e.mark)
        encodedLink.mkString(if (encodedLink.toString.contains(";")) SplitCharBackup else SplitChar)
      }
    }
  // Can't use the type alias because it messes up the type inference.
  protected implicit val markedLinksColumns: JdbcType[Iterable[MarkedLink[R]]] =
    new ColumnMappersSpecVer().iterable
  protected def toFreshness(o: Option[LocalDateTime]): Freshness =
    o.mapHeadOrElse(DatedFreshness, AlwaysFresh)
  protected override type Id = String
  protected implicit override def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  protected override def extractId(r: R) = r.normalize
  override def load(r: R) = super.load(r).mapError { case InvalidEntry(e) =>
    new AssertionError(s"Encountered an invalid entry <$e> for entity <$r>")
  }
}

@Singleton
private class SlickArtistExternalStorage @Inject() (
    ec: ExecutionContext,
    dbP: DbProvider,
) extends SlickExternalStorage[Artist](ec, dbP)
    with ArtistExternalStorage {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  protected override type Entity = (String, MarkedLinks[Artist], Option[LocalDateTime])
  protected class Rows(tag: Tag) extends Table[Entity](tag, "artist_link") {
    def name = column[String]("name", O.PrimaryKey)
    def encodedLinks = column[MarkedLinks[Artist]]("encoded_links")
    def timestamp = column[Option[LocalDateTime]]("timestamp")
    def * = (name, encodedLinks, timestamp)
  }
  protected override type EntityTable = Rows
  protected override val tableQuery = TableQuery[EntityTable]
  protected override def toEntity(k: Artist, v: (MarkedLinks[Artist], Freshness)) =
    (k.normalize, v._1, v._2.localDateTime)
  protected override def toId(et: EntityTable) = et.name
  protected override def extractValue(e: Entity) = e._2 -> toFreshness(e._3)
}

@Singleton
private class SlickAlbumExternalStorage @Inject() (
    ec: ExecutionContext,
    dbP: DbProvider,
) extends SlickExternalStorage[Album](ec, dbP)
    with AlbumExternalStorage {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  protected override type Entity = (String, String, MarkedLinks[Album], Option[LocalDateTime])
  protected class Rows(tag: Tag) extends Table[Entity](tag, "album_link") {
    def albumArtist = column[String]("album_artist", O.PrimaryKey)
    def artist = column[String]("artist")
    def encodedLinks = column[MarkedLinks[Album]]("encoded_links")
    def timestamp = column[Option[LocalDateTime]]("timestamp")
    def artist_index = index("external_album_artist_index", artist)
    def * = (albumArtist, artist, encodedLinks, timestamp)
  }
  protected override type EntityTable = Rows
  protected override val tableQuery = TableQuery[EntityTable]
  protected override def toEntity(k: Album, v: (MarkedLinks[Album], Freshness)) =
    (k.normalize, k.artist.normalize, v._1, v._2.localDateTime)
  protected override def toId(et: EntityTable) = et.albumArtist
  protected override def extractValue(e: Entity) = e._3 -> toFreshness(e._4)

  // TODO CASCADE
  def deleteAllLinks(a: Artist): Future[Iterable[(String, MarkedLinks[Album], Freshness)]] = {
    val artistRows = tableQuery.filter(_.artist === a.normalize)
    val existingRows = db.run(
      artistRows
        .map(e => (e.albumArtist, e.encodedLinks, e.timestamp))
        .result
        .map(_.map(e => (e._1, e._2, toFreshness(e._3)))),
    )
    existingRows <<* db.run(artistRows.delete)
  }
}
