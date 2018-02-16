package backend.external

import java.time.LocalDateTime

import backend.RichTime._
import backend.Url
import backend.configs.Configuration
import backend.logging.LoggerProvider
import backend.recon.{Album, Artist, Reconcilable}
import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import common.storage.{ColumnMappers, StringSerializable}
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.JdbcType

import scala.concurrent.Future

private[external] abstract class SlickExternalStorage[R <: Reconcilable](implicit dbP: DbProvider, lp: LoggerProvider)
    extends SlickStorageTemplateFromConf[R, (MarkedLinks[R], Option[LocalDateTime])] with ExternalStorage[R] {
  private class OldStorageEntry extends Exception
  import this.profile.api._

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
  override def load(r: R): Future[Option[(MarkedLinks[R], Option[LocalDateTime])]] =
    super.load(r).recoverWith {
      case _: OldStorageEntry =>
        lp.logger.error(s"Encountered an old storage entry for entity $r; removing entry")
        // Using internalDelete since regular delete also loads which results in an infinite recursion.
        internalDelete(r).>|(None)
    }
}

private[backend] class ArtistExternalStorage(implicit _dbP: DbProvider, lp: LoggerProvider) extends
    SlickExternalStorage[Artist] {
  import this.profile.api._

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

private[backend] class AlbumExternalStorage(implicit _dbP: DbProvider, lp: LoggerProvider) extends
    SlickExternalStorage[Album] {
  import this.profile.api._

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



