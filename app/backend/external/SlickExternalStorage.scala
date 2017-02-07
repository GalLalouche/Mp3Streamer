package backend.external

import backend.Url
import backend.configs.Configuration
import backend.recon.{Album, Artist, Reconcilable}
import backend.storage.SlickStorageUtils
import common.rich.RichT._
import org.joda.time.DateTime

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

private[this] class Serializer[R <: Reconcilable] {
  private val splitChar = ';'
  private val splitString = ";;"
  private def encode(e: ExternalLink[R]): String =
    s"${e.host.name}$splitChar${e.host.url.address}$splitChar${e.link.address}"
  private def decode(s: String): ExternalLink[R] =
    s.split(splitChar).ensuring(_.length == 3).mapTo(e => ExternalLink[R](Url(e(2)), Host(e(0), Url(e(1)))))

  def toString(els: Links[R]): String = els map encode mkString splitString
  def fromString(s: String): Links[R] = s split splitString filterNot (_.isEmpty) map decode
}

private[backend] class ArtistExternalStorage(implicit c: Configuration) extends ExternalStorage[Artist]
    with ToFunctorOps with FutureInstances {
  import c.driver.api._

  private class Rows(tag: Tag) extends Table[(String, String, Option[Long])](tag, "ARTIST_LINKS") {
    def name = column[String]("KEY", O.PrimaryKey)
    def encodedLinks = column[String]("LINKS_STRING")
    def timestamp = column[Option[Long]]("TIMESTAMP")
    def * = (name, encodedLinks, timestamp)
  }
  private val rows = TableQuery[Rows]
  private val db = c.db
  private val serializer = new Serializer[Artist]

  override def load(k: Artist): Future[Option[(Links[Artist], Option[DateTime])]] =
    db.run(rows
        .filter(_.name === k.normalize)
        .map(e => e.encodedLinks -> e.timestamp)
        .result
        .map(_.headOption.map(_.mapTo(e => e._1.mapTo(serializer.fromString) -> e._2.map(new DateTime(_))))))
  override protected def internalForceStore(a: Artist, v: (Links[Artist], Option[DateTime])) =
    db.run(rows.insertOrUpdate(a.normalize, serializer.toString(v._1), v._2.map(_.getMillis)))
  override def internalDelete(k: Artist) =
    db.run(rows.filter(_.name === k.normalize).delete)
  override def utils = SlickStorageUtils(c)(rows)
}

private[backend] class AlbumExternalStorage(implicit c: Configuration) extends ExternalStorage[Album]
    with ToFunctorOps with FutureInstances {
  import c.driver.api._

  private class Rows(tag: Tag) extends Table[(String, String, String, Option[Long])](tag, "ALBUM_LINKS") {
    def album = column[String]("ALBUM", O.PrimaryKey)
    def artist = column[String]("ARTIST")
    def encodedLinks = column[String]("LINKS_STRING")
    def timestamp = column[Option[Long]]("TIMESTAMP")
    def artist_index = index("artist_index", artist)
    def * = (album, artist, encodedLinks, timestamp)
  }
  private val rows = TableQuery[Rows]
  private val db = c.db
  private val serializer = new Serializer[Album]

  override def load(k: Album): Future[Option[(Links[Album], Option[DateTime])]] =
    db.run(rows
        .filter(_.album === k.normalize)
        .map(e => e.encodedLinks -> e.timestamp)
        .result
        .map(_.headOption.map(_.mapTo(e => e._1.mapTo(serializer.fromString) -> e._2.map(new DateTime(_))))))
  override protected def internalForceStore(a: Album, v: (Links[Album], Option[DateTime])) =
    db.run(rows.insertOrUpdate(a.normalize, a.artist.normalize, serializer.toString(v._1), v._2.map(_.getMillis)))
  override def internalDelete(k: Album) =
    db.run(rows.filter(_.album === k.normalize).delete)
  override def utils = SlickStorageUtils(c)(rows)
  def deleteAllLinks(a: Artist): Future[Traversable[(String, Links[Album], Option[DateTime])]] = {
    val artistRows = rows.filter(_.artist === a.normalize)
    for (existingRows <- db.run(artistRows
        .map(e => (e.album, e.encodedLinks, e.timestamp))
        .result
        .map(_.map(_.mapTo(e => (e._1, serializer.fromString(e._2), e._3.map(new DateTime(_)))))));
         _ <- db.run(artistRows.delete)) yield existingRows
  }
}



