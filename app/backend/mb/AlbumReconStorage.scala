package backend.mb

import backend.recon.{Album, ReconID, ReconStorage}
import common.rich.RichT._
import slick.driver.SQLiteDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private object AlbumReconStorage extends ReconStorage[Album] {
  private class Albums(tag: Tag) extends Table[(String, Option[String], Boolean)](tag, "ALBUMS") {
    def name = column[String]("NAME", O.PrimaryKey)
    def musicBrainzId = column[Option[String]]("MUSIC_BRAINZ_ID")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def * = (name, musicBrainzId, isIgnored)
  }
  private val albums = TableQuery[Albums]
  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  def store(a: Album, id: Option[String]): Future[Boolean] =
    store(a, id.map(ReconID.apply) -> (false == id.isDefined))
  /** If an existing value exists, override it. */
  override protected def internalForceStore(a: Album, value: (Option[ReconID], Boolean)): Future[Unit] =
    db.run(albums.forceInsert(a.normalize, value._1.map(_.id), value._2)).map(e => Unit)
  /** Returns the value associated with the key, if one exists, or None. */
  override def load(a: Album): Future[Option[(Option[ReconID], Boolean)]] =
    db.run(albums
        .filter(_.name === a.normalize)
        .map(e => e.isIgnored -> e.musicBrainzId)
        .result
        .map(_.headOption.map(_.swap.mapTo(e => e._1.map(ReconID) -> e._2))))
}
