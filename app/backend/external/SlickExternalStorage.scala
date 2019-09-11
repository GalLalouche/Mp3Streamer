package backend.external

import java.time.LocalDateTime

import backend.{FutureOption, Url}
import backend.RichTime._
import backend.logging.Logger
import backend.recon.{Album, Artist, Reconcilable}
import backend.storage.{AlwaysFresh, DatedFreshness, DbProvider, Freshness, SlickStorageTemplateFromConf}
import javax.inject.Inject
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.JdbcType

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.option.optionInstance
import common.rich.func.ToMoreFoldableOps._
import common.rich.func.ToMoreMonadErrorOps._

import common.rich.RichT._
import common.storage.{ColumnMappers, StringSerializable}

// TODO replace with composition
private[external] abstract class SlickExternalStorage[R <: Reconcilable](
    ec: ExecutionContext,
    dbP: DbProvider,
    logger: Logger,
) extends SlickStorageTemplateFromConf[R, (MarkedLinks[R], Freshness)](ec, dbP) with ExternalStorage[R] {
  private case class InvalidEntry(entry: String) extends Exception
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  protected implicit val localDateTimeColumn: JdbcType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, Long](_.toMillis, _.toLocalDateTime)
  private implicit def markedLinkStringSerializable: StringSerializable[MarkedLink[R]] =
  // FIXME some URLs include ";". We solve this by using "-;-" in those rare cases, but one should find a less hacky hack.
    new StringSerializable[MarkedLink[R]] {
      private val SplitChar = ";"
      private val SplitCharBackup = "-;-"
      override def separator = ";;"
      override def parse(s: String): MarkedLink[R] = {
        val split = s.split(if (s.contains(SplitCharBackup)) SplitCharBackup else SplitChar)
            .ifNot(_.length == 4).thenThrow(InvalidEntry(s))
        MarkedLink[R](
          link = Url(split(2)),
          host = Host(name = split(0), url = Url(split(1))),
          isNew = split(3).toBoolean)
      }
      override def stringify(e: MarkedLink[R]): String = {
        val encodedLink = Vector(e.host.name, e.host.url.address, e.link.address, e.isNew)
        encodedLink.mkString(if (encodedLink.toString.contains(";")) SplitCharBackup else SplitChar)
      }
    }
  // Can't use the type alias because it messes up the type inference.
  protected implicit val markedLinksColumns: JdbcType[Traversable[MarkedLink[R]]] =
    new ColumnMappers().traversable
  protected def toFreshness(o: Option[LocalDateTime]): Freshness = o.mapHeadOrElse(DatedFreshness, AlwaysFresh)
  override protected type Id = String
  override protected implicit def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  override protected def extractId(r: R) = r.normalize
  override def load(r: R): FutureOption[(MarkedLinks[R], Freshness)] = super.load(r).mapError {
    case InvalidEntry(e) => new AssertionError(s"Encountered an invalid entry <$e> for entity <$r>")
  }
}

private[backend] class ArtistExternalStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    logger: Logger,
) extends SlickExternalStorage[Artist](ec, dbP, logger) {
  import profile.api._

  override protected type Entity = (String, MarkedLinks[Artist], Option[LocalDateTime])
  protected class Rows(tag: Tag) extends Table[Entity](tag, "ARTIST_LINKS") {
    def name = column[String]("KEY", O.PrimaryKey)
    def encodedLinks = column[MarkedLinks[Artist]]("LINKS_STRING")
    def timestamp = column[Option[LocalDateTime]]("TIMESTAMP")
    def * = (name, encodedLinks, timestamp)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: Artist, v: (MarkedLinks[Artist], Freshness)) =
    (k.normalize, v._1, v._2.localDateTime)
  override protected def toId(et: EntityTable) = et.name
  override protected def extractValue(e: Entity) = e._2 -> toFreshness(e._3)
}

private[backend] class AlbumExternalStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    logger: Logger,
) extends SlickExternalStorage[Album](ec, dbP, logger) {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  override protected type Entity = (String, String, MarkedLinks[Album], Option[LocalDateTime])
  protected class Rows(tag: Tag) extends Table[Entity](tag, "ALBUM_LINKS") {
    def album = column[String]("ALBUM", O.PrimaryKey)
    def artist = column[String]("ARTIST")
    def encodedLinks = column[MarkedLinks[Album]]("LINKS_STRING")
    def timestamp = column[Option[LocalDateTime]]("TIMESTAMP")
    def artist_index = index("artist_index", artist)
    def * = (album, artist, encodedLinks, timestamp)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: Album, v: (MarkedLinks[Album], Freshness)) =
    (k.normalize, k.artist.normalize, v._1, v._2.localDateTime)
  override protected def toId(et: EntityTable) = et.album
  override protected def extractValue(e: Entity) = e._3 -> toFreshness(e._4)

  def deleteAllLinks(a: Artist): Future[Traversable[(String, MarkedLinks[Album], Freshness)]] = {
    val artistRows = tableQuery.filter(_.artist === a.normalize)
    val existingRows = db.run(artistRows
        .map(e => (e.album, e.encodedLinks, e.timestamp))
        .result
        .map(_.map(e => (e._1, e._2, toFreshness(e._3)))))
    existingRows `<*ByName` db.run(artistRows.delete)
  }
}
