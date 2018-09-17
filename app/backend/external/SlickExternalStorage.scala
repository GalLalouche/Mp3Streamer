package backend.external

import java.time.LocalDateTime

import backend.{FutureOption, Url}
import backend.RichTime._
import backend.logging.Logger
import backend.recon.{Album, Artist, Reconcilable}
import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import common.storage.{ColumnMappers, StringSerializable}
import javax.inject.Inject
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.JdbcType

import scala.concurrent.{ExecutionContext, Future}

// TODO replace with composition
private[external] abstract class SlickExternalStorage[R <: Reconcilable](
    ec: ExecutionContext,
    dbP: DbProvider,
    logger: Logger,
) extends SlickStorageTemplateFromConf[R, (MarkedLinks[R], Option[LocalDateTime])](ec, dbP) with ExternalStorage[R] {
  private class OldStorageEntry extends Exception
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  protected implicit val localDateTimeColumn: JdbcType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, Long](_.toMillis, _.toLocalDateTime)
  private implicit def markedLinkStringSerializable: StringSerializable[MarkedLink[R]] =
    new StringSerializable[MarkedLink[R]] {
      private val splitChar = ";"
      override def separator = ";;"
      override def parse(s: String): MarkedLink[R] = {
        val split = s split splitChar
        if (split.length != 4)
          throw new OldStorageEntry
        MarkedLink[R](
          link = Url(split(2)),
          host = Host(name = split(0), url = Url(split(1))),
          isNew = split(3).toBoolean)
      }
      override def stringify(e: MarkedLink[R]): String =
        List(e.host.name, e.host.url.address, e.link.address, e.isNew) mkString splitChar
    }
  // Can't use the type alias because it messes up the type inference.
  protected implicit val markedLinksColumns: JdbcType[Traversable[MarkedLink[R]]] =
    new ColumnMappers().traversable
  override protected type Id = String
  override protected implicit def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  override protected def extractId(r: R) = r.normalize
  override def load(r: R): FutureOption[(MarkedLinks[R], Option[LocalDateTime])] =
    super.load(r).recoverWith {
      case _: OldStorageEntry =>
        logger.error(s"Encountered an old storage entry for entity $r; removing entry")
        // Using internalDelete since regular delete also loads which results in an infinite recursion.
        internalDelete(r).>|(None)
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
  override protected def toEntity(k: Artist, v: (MarkedLinks[Artist], Option[LocalDateTime])) =
    (k.normalize, v._1, v._2)
  override protected def toId(et: EntityTable) = et.name
  override protected def extractValue(e: Entity) = e._2 -> e._3
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
  override protected def toEntity(k: Album, v: (MarkedLinks[Album], Option[LocalDateTime])) =
    (k.normalize, k.artist.normalize, v._1, v._2)
  override protected def toId(et: EntityTable) = et.album
  override protected def extractValue(e: Entity) = e._3 -> e._4

  def deleteAllLinks(a: Artist): Future[Traversable[(String, MarkedLinks[Album], Option[LocalDateTime])]] = {
    val artistRows = tableQuery.filter(_.artist === a.normalize)
    val existingRows = db.run(artistRows
        .map(e => (e.album, e.encodedLinks, e.timestamp))
        .result
        .map(_.map(e => (e._1, e._2, e._3))))
    existingRows `<*ByName` db.run(artistRows.delete)
  }
}



