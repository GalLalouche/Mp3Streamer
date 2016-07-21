package controllers

import java.net.{URLDecoder, URLEncoder}

import common.Debug
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.path.RichFile._
import decoders.DbPowerampCodec
import models._
import play.api.libs.json.{JsArray, JsString}
import play.api.mvc._
import search.Jsonable._
import songs.SongSelector

/** Handles fetch requests of JSON information, and listens to directory changes. */
object Player extends Controller with Debug {
  private val musicFinder = RealLocations
  private var songSelector: SongSelector = SongSelector.listen(musicFinder)
  def update() = timed("Updating music") {
    songSelector = SongSelector listen musicFinder
  }
  //TODO hide this, shouldn't be a part of the controller
  update()

  private def toJson(s: Song) = {
    DbPowerampCodec ! s.file
    SongJsonifier.jsonify(s) +
        ("poster" -> JsString("/posters/" + Poster.getCoverArt(s).path)) +
        ("mp3" -> JsString("/stream/download/" + URLEncoder.encode(s.file.path, "UTF-8")))
  }

  def randomSong = Action {
    Ok(songSelector.randomSong |> toJson)
  }

  def album(path: String) = Action {
    Ok(Directory(URLDecoder.decode(path, "UTF-8")) |> Album.apply |> (_.songs.map(toJson)) |> JsArray.apply)
  }

  def song(path: String) = Action {
    Ok(path |> Utils.parseSong |> toJson)
  }

  def nextSong(path: String) = Action {
    Ok(path |> Utils.parseSong |> songSelector.followingSong |> toJson)
  }
}
