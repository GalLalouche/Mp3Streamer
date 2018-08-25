package backend.lyrics

import backend.configs.Configuration
import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import common.rich.func.ToMoreFoldableOps
import models.Song
import net.codingwell.scalaguice.InjectorExtensions._
import slick.ast.{BaseTypedType, ScalaBaseType}

import scala.concurrent.ExecutionContext

import scalaz.std.OptionInstances

class LyricsStorage(implicit c: Configuration, ec: ExecutionContext)
    extends SlickStorageTemplateFromConf[Song, Lyrics]
    with ToMoreFoldableOps with OptionInstances {
  import profile.api._

  val dbP: DbProvider = c.injector.instance[DbProvider]
  override protected type Profile = dbP.profile.type
  override protected type Id = String
  override protected implicit def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  override protected type Entity = (String, String, Option[String])
  // instrumental songs have NULL in lyrics
  protected class LyricsTable(tag: Tag) extends Table[Entity](tag, "LYRICS") {
    def song = column[String]("SONG", O.PrimaryKey)
    def source = column[String]("SOURCE")
    def lyrics = column[Option[String]]("LYRICS")
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
  override protected def extractId(s: Song) = s"${s.artistName} - ${s.title}"
  override protected def extractValue(e: Entity) =
    e._3.mapHeadOrElse(HtmlLyrics(e._2, _), Instrumental(e._2))
}
