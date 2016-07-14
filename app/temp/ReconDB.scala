//package temp
//
//import common.storage.LocalStorage
//import slick.driver.SQLiteDriver.api._
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//object ReconDB extends LocalStorage[Reconnable, Option[String]] {
//  private class Recons(tag: Tag) extends Table[(String, Option[String])](tag, "KP_RECONS") {
//    def key = column[String]("KEY", O.PrimaryKey)
//    def url = column[Option[String]]("URL")
//    def * = (key, url)
//  }
//  private val recons = TableQuery[Recons]
//  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
//  private def getKey(r: Reconnable): String = r match {
//    case Artist(name) => "artist_" + name
//    case Album(name, a, year) => s"album_$name-$a-year"
//    case Song(name, artist, album) => s"song_$name-$artist-$album"
//  }
//  override def store(r: Reconnable, url: Option[String]): Future[Unit] =
//    db.run(recons.+=(getKey(r), url)).map(e => ())
//  override def load(r: Reconnable): Future[Option[String]] =
//    db.run(recons.filter(_.key === getKey(r)).result)
//        .map(_.head._2)
//
//}
