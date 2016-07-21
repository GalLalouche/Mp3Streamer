package lyrics

import common.storage.{LocalStorage, NewLocalStorage}
import models.Song

import scala.concurrent.{ExecutionContext, Future}
import slick.driver.SQLiteDriver.api._

private class LyricsStorage(implicit ec: ExecutionContext) extends NewLocalStorage[Song, Lyrics] {
  // instrumental songs have NULL in lyrics
  private class LyricsTable(tag: Tag) extends Table[(String, String, Option[String])](tag, "LYRICS") {
    def song = column[String]("SONG", O.PrimaryKey)
    def source = column[String]("SOURCE")
    def lyrics = column[Option[String]]("LYRICS")
    def * = (song, source, lyrics)
  }
  private def normalize(s: Song): String = s"${s.artistName} - ${s.title}"
  private val lyrics = TableQuery[LyricsTable]
  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override protected def internalForceStore(s: Song, l: Lyrics) = {
    val (source, content) = l match {
      case Instrumental(source) => source -> None
      case HtmlLyrics(source, html) => source -> Some(html)
    }
    db.run(lyrics.forceInsert(normalize(s), source, content)).map(e => ())
  }
  override def newLoad(s: Song): Future[Option[Lyrics]] =
    db.run(lyrics
        .filter(_.song === normalize(s))
        .map(e => e.source -> e.lyrics)
        .result)
        .map(_.headOption.map(e => e._2 match {
          case None => Instrumental(e._1)
          case Some(content) => HtmlLyrics(e._1, content)
        }))
}
