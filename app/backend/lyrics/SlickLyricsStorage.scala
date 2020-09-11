package backend.lyrics

import backend.storage.{DbProvider, SlickSingleKeyColumnStorageTemplateFromConf}
import javax.inject.{Inject, Singleton}
import models.Song
import slick.ast.{BaseTypedType, ScalaBaseType}

import scala.concurrent.ExecutionContext

import scalaz.std.option.optionInstance
import common.rich.func.ToMoreFoldableOps._

@Singleton
private class SlickLyricsStorage @Inject()(
    ec: ExecutionContext,
    protected val dbP: DbProvider,
) extends SlickSingleKeyColumnStorageTemplateFromConf[Song, Lyrics](ec, dbP) with LyricsStorage {
  import profile.api._

  override protected type Profile = dbP.profile.type
  override protected type Id = String
  override protected implicit def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  override protected type Entity = (String, String, Option[String])
  // instrumental songs have NULL in lyrics
  protected class LyricsTable(tag: Tag) extends Table[Entity](tag, "lyrics") {
    def song = column[String]("song", O.PrimaryKey)
    def source = column[String]("source")
    def lyrics = column[Option[String]]("lyrics")
    def * = (song, source, lyrics)
  }
  override protected type EntityTable = LyricsTable
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: Song, l: Lyrics) = {
    val (source, content) = l match {
      case Instrumental(source) => source -> None
      case HtmlLyrics(source, html) => source -> Some(html)
    }
    (extractId(k), source, content)
  }
  override protected def toId(et: LyricsTable) = et.song
  override protected def extractId(s: Song) = s"${
    s.artistName
  } - ${
    s.title
  }"
  override protected def extractValue(e: Entity) =
    e._3.mapHeadOrElse(HtmlLyrics(e._2, _), Instrumental(e._2))
}
