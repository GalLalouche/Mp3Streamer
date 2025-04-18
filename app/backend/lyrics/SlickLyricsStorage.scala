package backend.lyrics

import backend.lyrics.LyricsUrl.{DefaultEmpty, ManualEmpty, OldData, Url}
import backend.storage.{DbProvider, SlickSingleKeyColumnStorageTemplateFromConf}
import com.google.inject.{Inject, Singleton}
import models.Song
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.JdbcType

import scala.concurrent.ExecutionContext

import common.rich.func.ToMoreFoldableOps._
import scalaz.std.option.optionInstance

import common.rich.RichT._

@Singleton
private class SlickLyricsStorage @Inject() (
    ec: ExecutionContext,
    protected val dbP: DbProvider,
) extends SlickSingleKeyColumnStorageTemplateFromConf[Song, Lyrics](ec, dbP)
    with LyricsStorage {
  import profile.api._

  protected override type Profile = dbP.profile.type
  protected override type Id = String
  protected implicit override def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  protected override type Entity = (String, String, Option[String], Option[LyricsUrl])
  // instrumental songs have NULL in lyrics
  protected class LyricsTable(tag: Tag) extends Table[Entity](tag, "lyrics") {
    def song = column[String]("song", O.PrimaryKey)
    def source = column[String]("source")
    def lyrics = column[Option[String]]("lyrics")
    def url = column[Option[LyricsUrl]]("url")
    def * = (song, source, lyrics, url)
  }
  private implicit val lyricsUrlColumn: JdbcType[LyricsUrl] =
    MappedColumnType.base[LyricsUrl, String](
      SlickLyricsStorage.stringify,
      SlickLyricsStorage.decode,
    )
  protected override type EntityTable = LyricsTable
  protected override val tableQuery = TableQuery[EntityTable]

  protected override def toEntity(k: Song, l: Lyrics) = {
    val (source, content, url) = l match {
      case Instrumental(source, url) => (source, None, url)
      case HtmlLyrics(source, html, url) => (source, Some(html), url)
    }
    (extractId(k), source, content, url.optFilter(_ != OldData))
  }
  protected override def toId(et: LyricsTable) = et.song
  protected override def extractId(s: Song) = s"${s.artistName} - ${s.title}"
  protected override def extractValue(e: Entity) = {
    val url = e._4.getOrElse(OldData)
    e._3.mapHeadOrElse(HtmlLyrics(e._2, _, url), Instrumental(e._2, url))
  }
}
private object SlickLyricsStorage {

  private def stringify(t: LyricsUrl): String = t match {
    case LyricsUrl.Url(url) => url.toStringPunycode
    case OldData => throw new UnsupportedOperationException("Should not serialize OldData")
    case e => e.toString
  }
  private def decode(s: String): LyricsUrl = s match {
    case "DefaultEmpty" => DefaultEmpty
    case "ManualEmpty" => ManualEmpty
    case e => Url(io.lemonlabs.uri.Url.parse(e))
  }
}
