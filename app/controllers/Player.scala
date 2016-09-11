package controllers

import java.net.URLEncoder

import common.Debug
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.path.RichFile._
import common.rich.primitives.RichEither._
import common.rich.primitives.RichOption._
import models._
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import play.api.mvc._
import search.Jsonable._
import songs.{SongGroup, SongGroups, SongSelector}

/** Handles fetch requests of JSON information, and listens to directory changes. */
object Player extends Controller with Debug {
  private implicit val c = PlayConfig
  import c._
  private val songGroups: Map[Song, SongGroup] = SongGroups.load |> SongGroups.fromGroups
  private var songSelector: SongSelector = _
  def update() = timed("Updating music") {
    songSelector = SongSelector listen c.mf
  }
  //TODO hide this, shouldn't be a part of the controller
  update()

  private def toJson(s: Song): JsObject = {
    SongJsonifier.jsonify(s) +
        ("poster" -> JsString("/posters/" + Poster.getCoverArt(s).path)) +
        ("mp3" -> JsString("/stream/download/" + URLEncoder.encode(s.file.path, "UTF-8")))
  }
  private def toJson(ss: SongGroup): JsArray = ss.songs map toJson mapTo JsArray
  private def toJson(e: Either[Song, SongGroup]): JsValue = e.resolve(toJson, toJson)

  private def group(s: Song): Either[Song, SongGroup] = songGroups get s either s

  def randomSong = Action {
    Ok(songSelector.randomSong |> group |> toJson)
  }

  def album(path: String) = Action {
    Ok(Utils.parseFile(path) |> Directory.apply |> Album.apply |> (_.songs.map(toJson)) |> JsArray.apply)
  }

  def song(path: String) = Action {
    Ok(path |> Utils.parseSong |> group |> toJson)
  }

  def nextSong(path: String) = Action {
    Ok(path |> Utils.parseSong |> songSelector.followingSong |> toJson)
  }
}
