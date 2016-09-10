package songs

import backend.configs.StandaloneConfig
import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import models.Song
import play.api.libs.json.{JsArray, JsObject, Json}
import search.Jsonable

import scala.concurrent.{ExecutionContext, Future}

object SongGroups {
  def fromGroups(groups: Traversable[SongGroup]): Map[Song, SongGroup] =
    groups.foldLeft(Map[Song, SongGroup]())(_ ++ _.mapTo(e => e.songs.map(_ -> e)).toMap)

  private def getJsonFile(implicit root: DirectoryRef, ec: ExecutionContext): Future[FileRef] =
    Future(root addFile "song_groups.json")
  private def writeToJsonFile(s: String)(implicit root: DirectoryRef, ec: ExecutionContext): Future[Unit] =
    getJsonFile.map(_ write s)
  def save(groups: Traversable[SongGroup])(implicit root: DirectoryRef, ec: ExecutionContext): Future[Unit] = groups
      .map(_.songs.map(Jsonable.SongJsonifier.jsonify) |> JsArray)
      .map(_.toString)
      .mkString("\n") |> writeToJsonFile
  def load(implicit root: DirectoryRef, ec: ExecutionContext): Future[Traversable[SongGroup]] = getJsonFile.map(_.lines
      .map(Json.parse)
      .map(_.as[JsArray].value.map(_.as[JsObject] |> Jsonable.SongJsonifier.parse) |> SongGroup))

  // Appends new groups and saves them
  def main(args: Array[String]): Unit = {
    import common.rich.RichFuture._
    implicit val c = StandaloneConfig
    import c._
    def append(g: SongGroup): Future[Unit] =
      load.map(g :: _.toList).flatMap(save)
    val group: SongGroup = ???
    append(group).get
    println("Done")
  }
}
