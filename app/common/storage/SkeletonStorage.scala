//package common.storage
//
//import common.RichFuture._
//import mains.albums.{ID, ReconStorage}
//import slick.driver.SQLiteDriver.api._
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//abstract class SkeletonStorage[K, V](tableName: String) extends LocalStorage[K, V] {
//  type table <: Table
//  private class BasicTable(tag: Tag) extends Table[(K, V)](tag, tableName) {
//    def key = column[K]("KEY", O.PrimaryKey)
//    def value = column[]
//  }
////    def name = column[String]("NAME", O.PrimaryKey)
////    def musicBrainzId = column[Option[String]]("MUSIC_BRAINZ_ID")
////    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
////    def * = (name, musicBrainzId, isIgnored)
////  }
//  private val artists = TableQuery[Artists]
//  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
//  override def store(artistName: String, id: Option[ID]) =
//    db.run(artists.+=(normalize(artistName), id, false == id.isDefined)).map(e => ())
//  override def load(artistName: String): Future[Option[ID]] =
//    db.run(artists
//        .filter(_.name === normalize(artistName))
//        .map(e => e.isIgnored -> e.musicBrainzId)
//        .result
//    ).filterWithMessage(_.nonEmpty, e => s"Could not find a match for key <$artistName>")
//        .map(_.head) // returns the first result (artistName is primary key, so it's ok)
//        .map(e => if (e._1) None else e._2) // if is ignored, return None, else return the key stored (which may also be None)
//
//  def main(args: Array[String]) {
//    println(load("zz top").get)
//  }
//
//}
