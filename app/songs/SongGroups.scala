package songs

import backend.configs.StandaloneConfig
import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import models.Song
import play.api.libs.json.{JsArray, JsObject, Json}
import search.Jsonable

import scala.concurrent.ExecutionContext

object SongGroups {
  def fromGroups(groups: Traversable[SongGroup]): Map[Song, SongGroup] =
    groups.foldLeft(Map[Song, SongGroup]())(_ ++ _.mapTo(e => e.songs.map(_ -> e)).toMap)

  private def getJsonFile(implicit root: DirectoryRef, ec: ExecutionContext): FileRef =
    root addFile "song_groups.json"
  private def writeToJsonFile(s: String)(implicit root: DirectoryRef, ec: ExecutionContext) =
    getJsonFile write s
  def save(groups: Traversable[SongGroup])(implicit root: DirectoryRef, ec: ExecutionContext) = groups
      .map(_.songs.map(Jsonable.SongJsonifier.jsonify) |> JsArray)
      .map(_.toString)
      .mkString("\n") |> writeToJsonFile
  def load(implicit root: DirectoryRef, ec: ExecutionContext): Traversable[SongGroup] = getJsonFile.lines
      .map(Json.parse)
      .map(_.as[JsArray].value.map(_.as[JsObject] |> Jsonable.SongJsonifier.parse) |> SongGroup)

  // Appends new groups and saves them
  def main(args: Array[String]): Unit = {
    implicit val c = StandaloneConfig
    import c._
    def append(g: SongGroup) = (g :: load.toList) |> save
    val group: SongGroup = ???
    append(group)
    println("Done")
  }
}
