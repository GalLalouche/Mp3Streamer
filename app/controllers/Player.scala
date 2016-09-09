package controllers

import java.net.{URLDecoder, URLEncoder}

import common.Debug
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.path.RichFile._
import decoders.DbPowerampCodec
import models._
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import play.api.mvc._
import search.Jsonable._
import songs.{SongGroup, SongGroups, SongSelector}
import common.rich.primitives.RichOption._
import common.rich.primitives.RichEither._

/** Handles fetch requests of JSON information, and listens to directory changes. */
object Player extends Controller with Debug {
  private implicit val c = PlayConfig
  import c._
  private val musicFinder = RealLocations
  private val songGroups: Map[Song, SongGroup] = SongGroups.load map SongGroups.fromGroups get
  private var songSelector: SongSelector = SongSelector.listen(musicFinder)
  def update() = timed("Updating music") {
    songSelector = SongSelector listen musicFinder
  }
  //TODO hide this, shouldn't be a part of the controller
  update()

  private def toJson(s: Song): JsObject = {
    DbPowerampCodec ! s.file
    SongJsonifier.jsonify(s) +
        ("poster" -> JsString("/posters/" + Poster.getCoverArt(s).path)) +
        ("mp3" -> JsString("/stream/download/" + URLEncoder.encode(s.file.path, "UTF-8")))
  }
  private def toJson(ss: SongGroup): JsArray = ss.songs.toSeq map toJson mapTo JsArray
  private def toJson(e: Either[Song, SongGroup]): JsValue = e.resolve(toJson, toJson)

  private def group(s: Song): Either[Song, SongGroup] = songGroups get s mapTo (_ either s) swap

  def randomSong = Action {
    Ok(songSelector.randomSong |> group |> toJson)
  }

  def album(path: String) = Action {
    Ok(Directory(URLDecoder.decode(path, "UTF-8")) |> Album.apply |> (_.songs.map(toJson)) |> JsArray.apply)
  }

  def song(path: String) = Action {
    Ok(path |> Utils.parseSong |> group |> toJson)
  }

  def nextSong(path: String) = Action {
    Ok(path |> Utils.parseSong |> songSelector.followingSong |> toJson)
  }
}
