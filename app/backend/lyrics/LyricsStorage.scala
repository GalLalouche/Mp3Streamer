package backend.lyrics

import backend.configs.Configuration
import backend.storage.{LocalStorageTemplate, LocalStorageUtils, SlickLocalStorageUtils}
import models.Song

import scala.concurrent.Future

private class LyricsStorage(implicit c: Configuration) extends LocalStorageTemplate[Song, Lyrics] {
  import c.driver.api._
  private val db = c.db

  // instrumental songs have NULL in lyrics
  private class LyricsTable(tag: Tag) extends Table[(String, String, Option[String])](tag, "LYRICS") {
    def song = column[String]("SONG", O.PrimaryKey)
    def source = column[String]("SOURCE")
    def lyrics = column[Option[String]]("LYRICS")
    def * = (song, source, lyrics)
  }
  private def normalize(s: Song): String = s"${s.artistName} - ${s.title}"
  private val rows = TableQuery[LyricsTable]
  override protected def internalForceStore(s: Song, l: Lyrics) = {
    val (source, content) = l match {
      case Instrumental(source) => source -> None
      case HtmlLyrics(source, html) => source -> Some(html)
    }
    db.run(rows.insertOrUpdate(normalize(s), source, content)).map(e => ())
  }
  override def load(s: Song): Future[Option[Lyrics]] =
    db.run(rows
        .filter(_.song === normalize(s))
        .map(e => e.source -> e.lyrics)
        .result
    ).map(_.headOption.map(e => e._2
        .map(HtmlLyrics(e._1, _))
        .getOrElse(Instrumental(e._1))))
  override def utils: LocalStorageUtils = SlickLocalStorageUtils(c)(rows)
}
