package backend.lyrics

import backend.configs.Configuration
import backend.storage.SlickStorageTemplate
import models.Song
import slick.ast.{BaseTypedType, ScalaBaseType}

class LyricsStorage(implicit _c: Configuration) extends SlickStorageTemplate[Song, Lyrics] {
  import c.profile.api._

  private def normalize(s: Song): String = s"${s.artistName} - ${s.title}"

  override protected type Id = String
  override protected type Entity = (String, String, Option[String])

  // instrumental songs have NULL in lyrics
  protected class LyricsTable(tag: Tag) extends Table[Entity](tag, "LYRICS") {
    def song = column[String]("SONG", O.PrimaryKey)
    def source = column[String]("SOURCE")
    def lyrics = column[Option[String]]("LYRICS")
    def * = (song, source, lyrics)
  }
  override protected type EntityTable = LyricsTable
  override protected val tableQuery = TableQuery(new LyricsTable(_))
  override protected def toEntity(k: Song, l: Lyrics) = {
    val (source, content) = l match {
      case Instrumental(source) => source -> None
      case HtmlLyrics(source, html) => source -> Some(html)
    }
    (normalize(k), source, content)
  }
  override protected def toId(et: LyricsTable) = et.song
  override protected def extractId(k: Song) = normalize(k)
  override protected def extractValue(e: Entity) = e._3.map(HtmlLyrics(e._2, _)).getOrElse(Instrumental(e._2))
  override protected implicit def btt: BaseTypedType[String] = ScalaBaseType.stringType
}
