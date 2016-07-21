package backend.mb

import backend.recon.{Artist, ReconID, ReconStorage}
import common.RichFuture._
import slick.driver.SQLiteDriver.api._

import scala.concurrent.{ExecutionContext, Future}

class ArtistReconStorage(implicit ec: ExecutionContext) extends ReconStorage[Artist] {
  private class Artists(tag: Tag) extends Table[(String, Option[String], Boolean)](tag, "ARTISTS") {
    def name = column[String]("NAME", O.PrimaryKey)
    def musicBrainzId = column[Option[String]]("MUSIC_BRAINZ_ID")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def * = (name, musicBrainzId, isIgnored)
  }
  override protected def normalize(a: Artist): String = a.name.toLowerCase
  private val artists = TableQuery[Artists]
  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  def store(a: Artist, id: Option[String]): Future[Unit] =
    store(a, id.map(ReconID.apply) -> (false == id.isDefined))
  override def store(a: Artist, value: (Option[ReconID], Boolean)) =
    db.run(artists.+=((normalize(a), value._1.map(_.id), value._2))).map(e => ())
  override def load(a: Artist): Future[(Option[ReconID], Boolean)] =
    db.run(artists
      .filter(_.name === normalize(a))
      .map(e => e.isIgnored -> e.musicBrainzId)
      .result
    ).filterWithMessage(_.nonEmpty, e => s"Could not find a match for key <$a>")
      .map(_.head.swap) // returns the first result (artistName is primary key, so it's ok)
      .map(e => e._1.map(ReconID) -> e._2)
}
