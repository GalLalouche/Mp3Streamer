package songs

import backend.configs.StandaloneConfig
import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.path.Directory
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
  def load(implicit root: DirectoryRef, ec: ExecutionContext): Set[SongGroup] = getJsonFile.lines
      .map(Json.parse)
      .map(_.as[JsArray].value.map(_.as[JsObject] |> Jsonable.SongJsonifier.parse) |> SongGroup)
      .toSet

  // Appends new groups and saves them
  def main(args: Array[String]): Unit = {
    def trackNumbers(directory: String, trackNumbers: Int*): SongGroup = {
      val dir = Directory(directory)
      val prefixes: Set[String] = trackNumbers.map(_.toString.mapIf(_.length < 2).to("0".+)).toSet
      def isPrefix(s: String) = prefixes exists s.startsWith
      val songs = dir.files.filter(_.getName |> isPrefix)
      SongGroup(songs.sortBy(_.getName).map(Song.apply))
    }
    implicit val c = StandaloneConfig
    import c._
    def append(g: SongGroup) = (g :: load.toList).toSet |> save
    val group: SongGroup = trackNumbers("""D:\Media\Music\Rock\Soft Rock\Regina Spektor\2005 Soviet Kitscsh""", 7, 8)
    append(group)
    println("Done")
  }
}
