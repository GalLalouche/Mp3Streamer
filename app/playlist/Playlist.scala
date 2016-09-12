package playlist

import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import models.Song
import play.api.libs.json.{JsArray, Json}
import search.Jsonable.SongJsonifier

case class Playlist(songs: Seq[Song])

object Playlist {
  private def jsonFile(implicit root: DirectoryRef): FileRef = root addSubDir "data" addFile "playlist.json"
  def save(playlist: Playlist)(implicit root: DirectoryRef): Unit = {
    playlist.songs |> SongJsonifier.jsonify |> (_.toString) |> jsonFile.write
  }

  def load(implicit root: DirectoryRef): Playlist =
    jsonFile.readAll.ensuring(_.nonEmpty, "No playlist saved") mapTo Json.parse mapTo (_.as[JsArray] |> SongJsonifier.parse |> Playlist.apply)
}
