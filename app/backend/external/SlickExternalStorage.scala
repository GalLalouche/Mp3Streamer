package backend.external

import java.time.LocalDateTime

import backend.RichTime._
import backend.Url
import backend.configs.Configuration
import backend.recon.{Album, Artist, Reconcilable}
import backend.storage.SlickStorageTemplate
import common.rich.RichT._
import slick.ast.{BaseTypedType, ScalaBaseType}

import scala.concurrent.Future

private[this] class OldStorageEntry extends Exception
private[this] class Serializer[R <: Reconcilable] {
  private val splitChar = ";"
  private val splitString = ";;"
  // TODO handle lists in a less hacky way
  private def encode(e: MarkedLink[R]): String =
    List(e.host.name, e.host.url.address, e.link.address, e.isNew) mkString splitChar
  private def decode(s: String): MarkedLink[R] = {
    val split = s split splitChar
    if (split.length != 4)
      throw new OldStorageEntry
    split.mapTo(e => MarkedLink[R](
      link = Url(e(2)),
      host = Host(name = e(0), url = Url(e(1))),
      isNew = e(3).toBoolean))
  }

  def toString(els: MarkedLinks[R]): String = els map encode mkString splitString
  def fromString(s: String): MarkedLinks[R] = s split splitString filterNot (_.isEmpty) map decode
}

private[external] abstract class SlickExternalStorage[R <: Reconcilable](implicit _c: Configuration)
    extends SlickStorageTemplate[R, (MarkedLinks[R], Option[LocalDateTime])] with ExternalStorage[R] {
  override protected def extractId(r: R) = r.normalize

  override def load(r: R): Future[Option[(MarkedLinks[R], Option[LocalDateTime])]] =
    super.load(r).recoverWith {
      case _: OldStorageEntry =>
        c.logger.error(s"Encountered an old storage entry for entity $r; removing entry")
        // Using internalDelete since regular delete also loads which results in an infinite recursion.
        internalDelete(r).>|(None)
    }
}

private[backend] class ArtistExternalStorage(implicit _c: Configuration) extends
    SlickExternalStorage[Artist] {
  import c.profile.api._

  private val serializer = new Serializer[Artist]

  override protected type Entity = (String, String, Option[Long])
  protected class Rows(tag: Tag) extends Table[Entity](tag, "ARTIST_LINKS") {
    def name = column[String]("KEY", O.PrimaryKey)
    def encodedLinks = column[String]("LINKS_STRING")
    def timestamp = column[Option[Long]]("TIMESTAMP")
    def * = (name, encodedLinks, timestamp)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: Artist, v: (MarkedLinks[Artist], Option[LocalDateTime])) =
    (k.normalize, serializer.toString(v._1), v._2.map(_.toMillis))
  override protected def toId(et: EntityTable) = et.name
  override protected def extractValue(e: Entity) = serializer.fromString(e._2) -> e._3.map(_.toLocalDateTime)
}

private[backend] class AlbumExternalStorage(implicit _c: Configuration) extends
    SlickExternalStorage[Album] {
  import c.profile.api._

  private val serializer = new Serializer[Album]
  override protected type Entity = (String, String, String, Option[Long])
  protected class Rows(tag: Tag) extends Table[Entity](tag, "ALBUM_LINKS") {
    def album = column[String]("ALBUM", O.PrimaryKey)
    def artist = column[String]("ARTIST")
    def encodedLinks = column[String]("LINKS_STRING")
    def timestamp = column[Option[Long]]("TIMESTAMP")
    def artist_index = index("artist_index", artist)
    def * = (album, artist, encodedLinks, timestamp)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: Album, v: (MarkedLinks[Album], Option[LocalDateTime])) =
    (k.normalize, k.artist.normalize, serializer.toString(v._1), v._2.map(_.toMillis))
  override protected def toId(et: EntityTable) = et.album
  override protected def extractValue(e: Entity) = serializer.fromString(e._3) -> e._4.map(_.toLocalDateTime)
  def deleteAllLinks(a: Artist): Future[Traversable[(String, MarkedLinks[Album], Option[LocalDateTime])]] = {
    val artistRows = tableQuery.filter(_.artist === a.normalize)
    for (existingRows <- db.run(artistRows
        .map(e => (e.album, e.encodedLinks, e.timestamp))
        .result
        .map(_.map(e => (e._1, serializer.fromString(e._2), e._3.map(_.toLocalDateTime)))));
         _ <- db.run(artistRows.delete)) yield existingRows
  }
}



