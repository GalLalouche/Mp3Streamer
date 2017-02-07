package backend.recon

import backend.configs.Configuration
import backend.storage.SlickStorageUtils
import common.rich.RichT._

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

//TODO so much code duplication :(
class ArtistReconStorage(implicit c: Configuration) extends ReconStorage[Artist]
    with ToFunctorOps with FutureInstances {
  import c.driver.api._

  private class Rows(tag: Tag) extends Table[(String, Option[String], Boolean)](tag, "ARTISTS") {
    def name = column[String]("KEY", O.PrimaryKey)
    def reconId = column[Option[String]]("RECON_ID")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def * = (name, reconId, isIgnored)
  }
  private val rows = TableQuery[Rows]
  private val db = c.db
  def store(a: Artist, id: Option[String]): Future[Boolean] =
    store(a, id.map(ReconID.apply) -> id.isEmpty)
  override protected def internalForceStore(a: Artist, value: (Option[ReconID], Boolean)) =
    db.run(rows.insertOrUpdate(a.normalize, value._1.map(_.id), value._2))
  override def load(a: Artist): Future[Option[(Option[ReconID], Boolean)]] =
    db.run(rows
        .filter(_.name === a.normalize)
        .map(e => e.isIgnored -> e.reconId)
        .result
        .map(_.headOption.map(_.swap.mapTo(e => e._1.map(ReconID) -> e._2))))
  override def internalDelete(a: Artist) =
    db.run(rows.filter(_.name === a.normalize).delete)
  override def utils = SlickStorageUtils(c)(rows)
}

class AlbumReconStorage(implicit c: Configuration) extends ReconStorage[Album]
    with ToFunctorOps with FutureInstances {
  import c.driver.api._
  private class Rows(tag: Tag) extends Table[(String, String, Option[String], Boolean)](tag, "ALBUMS") {
    def album = column[String]("ALBUM", O.PrimaryKey)
    def artist = column[String]("ARTIST")
    def reconId = column[Option[String]]("RECON_ID")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def artistIndex = index("ARTIST_INDEX", artist)
    def * = (album, artist, reconId, isIgnored)
  }
  private val rows = TableQuery[Rows]
  private val db = c.db
  override protected def internalForceStore(a: Album, value: (Option[ReconID], Boolean)) =
    db.run(rows.insertOrUpdate(a.normalize, a.artist.normalize, value._1.map(_.id), value._2))
  override def load(a: Album): Future[Option[(Option[ReconID], Boolean)]] =
    db.run(rows
        .filter(_.album === a.normalize)
        .map(e => e.isIgnored -> e.reconId)
        .result
        .map(_.headOption.map(_.swap.mapTo(e => e._1.map(ReconID) -> e._2))))
  override def internalDelete(a: Album) =
    db.run(rows.filter(_.album === a.normalize).delete)
  override def utils = SlickStorageUtils(c)(rows)
  /** Delete all recons for albums for that artist */
  def deleteAllRecons(a: Artist): Future[Traversable[(String, Option[ReconID], Boolean)]] = {
    val artistRows = rows.filter(_.artist === a.normalize)
    for (existingRows <- db.run(artistRows
        .map(e => (e.album, e.reconId, e.isIgnored))
        .result
        .map(_.map(_.mapTo(e => (e._1, e._2.map(ReconID), e._3)))));
         _ <- db.run(artistRows.delete)) yield existingRows
  }
}
