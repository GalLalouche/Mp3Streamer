package backend.mb

import backend.recon.{Album, ReconID, ReconStorage}
import common.RichFuture._
import slick.driver.SQLiteDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private object AlbumReconStorage extends ReconStorage[Album] {
  private class Albums(tag: Tag) extends Table[(String, Option[String], Boolean)](tag, "ALBUMS") {
    def name = column[String]("NAME", O.PrimaryKey)
    def musicBrainzId = column[Option[String]]("MUSIC_BRAINZ_String")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def * = (name, musicBrainzId, isIgnored)
  }
  override protected def normalize(a: Album): String = s"${a.artistName}-${a.title}".toLowerCase
  private val artists = TableQuery[Albums]
  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  def store(a: Album, id: Option[String]): Future[Unit] =
    store(a, id.map(ReconID.apply) -> (false == id.isDefined))
  override def store(a: Album, value: (Option[ReconID], Boolean)) =
    db.run(artists.+=((normalize(a), value._1.map(_.id), value._2))).map(e => ())
  override def load(a: Album): Future[(Option[ReconID], Boolean)] =
    db.run(artists
      .filter(_.name === normalize(a))
      .map(e => e.isIgnored -> e.musicBrainzId)
      .result
    ).filterWithMessage(_.nonEmpty, e => s"Could not find a match for key <$a>")
      .map(_.head.swap) // returns the first result (artistName is primary key, so it's ok)
      .map(e => e._1.map(ReconID) -> e._2)
}
