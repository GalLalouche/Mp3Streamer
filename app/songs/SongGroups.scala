package songs

import common.Jsonable
import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import models.Song
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.ExecutionContext

class SongGroups(implicit songJsonable: Jsonable[Song]) {
  private def getJsonFile(implicit root: DirectoryRef, ec: ExecutionContext): FileRef =
    root addFile "song_groups.json"
  private def writeToJsonFile(s: String)(implicit root: DirectoryRef, ec: ExecutionContext) =
    getJsonFile write s
  def save(groups: Traversable[SongGroup])(implicit root: DirectoryRef, ec: ExecutionContext): Unit =
    groups
        .map(_.songs |> songJsonable.jsonify)
        .map(_.toString)
        .mkString("\n") |> writeToJsonFile
  def load(implicit root: DirectoryRef, ec: ExecutionContext): Set[SongGroup] = getJsonFile.lines
      .map(Json.parse)
      .map(_.as[JsArray] |> songJsonable.parse |> SongGroup)
      .toSet
}

object SongGroups {
  def fromGroups(groups: Traversable[SongGroup]): Map[Song, SongGroup] =
    groups.foldLeft(Map[Song, SongGroup]())(_ ++ _.mapTo(e => e.songs.map(_ -> e)).toMap)
}
