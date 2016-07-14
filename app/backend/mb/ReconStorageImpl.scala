package backend.mb

import backend.recon.ReconStorage
import common.RichFuture._
import slick.driver.SQLiteDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private object ReconStorageImpl extends ReconStorage {
  private class Artists(tag: Tag) extends Table[(String, Option[String], Boolean)](tag, "ARTISTS") {
    def name = column[String]("NAME", O.PrimaryKey)
    def musicBrainzId = column[Option[String]]("MUSIC_BRAINZ_String")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def * = (name, musicBrainzId, isIgnored)
  }
  private val artists = TableQuery[Artists]
  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  def store(artistName: String, id: Option[String]): Future[Unit] = store(artistName, id -> (false == id.isDefined))
  override def store(artistName: String, value: (Option[String], Boolean)) =
    db.run(artists.+=((normalize(artistName), value._1, value._2))).map(e => ())
  override def load(artistName: String): Future[(Option[String], Boolean)] =
    db.run(artists
        .filter(_.name === normalize(artistName))
        .map(e => e.isIgnored -> e.musicBrainzId)
        .result
    ).filterWithMessage(_.nonEmpty, e => s"Could not find a match for key <$artistName>")
        .map(_.head.swap) // returns the first result (artistName is primary key, so it's ok)

  def main(args: Array[String]) {
    println(load("zz top").get)
  }
}
