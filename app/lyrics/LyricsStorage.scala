package lyrics

import common.storage.LocalStorage
import models.Song

import scala.concurrent.Future
import slick.driver.SQLiteDriver.api._
import common.RichFuture._
import scala.concurrent.ExecutionContext.Implicits.global

private object LyricsStorage extends LocalStorage[Song, Lyrics] {
  private class Artists(tag: Tag) extends Table[(String, String, String)](tag, "LYRICS") {
    def song = column[String]("SONG", O.PrimaryKey)
    def source = column[String]("SOURCE")
    def lyrics = column[String]("LYRICS")
    def * = (song, source, lyrics)
  }
  private def normalize(s: Song): String = s"${s.artistName} - ${s.title}"
  private val lyrics = TableQuery[Artists]
  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override def store(s: Song, l: Lyrics) =
    db.run(lyrics.+=(normalize(s), l.source, l.html)).map(e => ())
  override def load(s: Song): Future[Lyrics] =
    db.run(lyrics
        .filter(_.song === normalize(s))
        .map(e => e.lyrics -> e.source)
        .result)
        .map(_.head)
        .map(e => new Lyrics(e._1, e._2))

  def main(args: Array[String]) {
    (db run lyrics.schema.create).get
  }
}
