package songs

import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import models.Song
import play.api.libs.json.{JsArray, JsObject, Json}
import search.Jsonable

import scala.concurrent.{ExecutionContext, Future}

private object SongGroups {
  def fromGroups(groups: Traversable[SongGroup]): Map[Song, SongGroup] =
    groups.foldLeft(Map[Song, SongGroup]())(_ ++ _.mapTo(e => e.songs.map(_ -> e)).toMap)

  private def getJsonFile(implicit root: DirectoryRef, ec: ExecutionContext): Future[FileRef] =
    Future(root addFile "song_groups.json")
  private def writeToJsonFile(s: String)(implicit root: DirectoryRef, ec: ExecutionContext): Future[Unit] =
    getJsonFile.map(_ write s)
  def save(groups: Traversable[SongGroup])(implicit root: DirectoryRef, ec: ExecutionContext): Future[Unit] =
    groups
        .map(_.songs.map(Jsonable.SongJsonifier.jsonify).toSeq |> JsArray)
        .map(_.toString).mkString("\n") |> writeToJsonFile
  def load(implicit root: DirectoryRef, ec: ExecutionContext): Future[Traversable[SongGroup]] = getJsonFile.map(
    _.lines.map(Json.parse).map(_.as[JsArray].value.map(_.as[JsObject] |> Jsonable.SongJsonifier.parse)).map(_.toSet |> SongGroup))
}
