package backend.mb

import backend.recon.{Artist, ReconID, ReconStorage}
import common.rich.RichT._
import slick.driver.SQLiteDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private object ArtistReconStorage extends ReconStorage[Artist] {
  private class Artists(tag: Tag) extends Table[(String, Option[String], Boolean)](tag, "ARTISTS") {
    def name = column[String]("NAME", O.PrimaryKey)
    def musicBrainzId = column[Option[String]]("MUSIC_BRAINZ_String")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def * = (name, musicBrainzId, isIgnored)
  }
  override protected def normalize(a: Artist): String = a.name.toLowerCase
  private val artists = TableQuery[Artists]
  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  def store(a: Artist, id: Option[String]): Future[Unit] =
    store(a, id.map(ReconID.apply) -> (false == id.isDefined))
  override def newLoad(a: Artist): Future[Option[(Option[ReconID], Boolean)]] =
    db.run(artists
      .filter(_.name === normalize(a))
      .map(e => e.isIgnored -> e.musicBrainzId)
      .result
      .map(_.headOption.map(_.swap.mapTo(e => e._1.map(ReconID) -> e._2))))
  override protected def internalForceStore(a: Artist, value: (Option[ReconID], Boolean)): Future[Unit] =
    db.run(artists.forceInsert(normalize(a), value._1.map(_.id), value._2)).map(e => Unit)
}
