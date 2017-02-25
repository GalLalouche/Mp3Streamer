package controllers

import common.Debug
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.primitives.RichEither._
import models._
import play.api.libs.json.{JsArray, JsValue}
import play.api.mvc._
import songs.{SongGroup, SongGroups, SongSelector}

/** Handles fetch requests of JSON information, and listens to directory changes. */
object Player extends Controller with Debug {
  private implicit val c = Utils.config
  import c._
  private val songGroups: Map[Song, SongGroup] = SongGroups.load |> SongGroups.fromGroups
  private var songSelector: SongSelector = _
  def update() = {
    songSelector = SongSelector.create
  }
  //TODO hide this, shouldn't be a part of the controller
  update()

  private def toJson(ss: SongGroup): JsArray = ss.songs map Utils.toJson mapTo JsArray
  private def toJson(e: Either[Song, SongGroup]): JsValue = e.resolve(Utils.toJson, toJson)

  private def group(s: Song): Either[Song, SongGroup] = songGroups get s toRight s

  def randomSong = Action {
    Ok(songSelector.randomSong |> group |> toJson)
  }

  def album(path: String) = Action {
    Ok(Utils.parseFile(path) |> Directory.apply |> Album.apply |> (_.songs.map(Utils.toJson)) |> JsArray.apply)
  }

  def song(path: String) = Action {
    Ok(path |> Utils.parseSong |> group |> toJson)
  }

  def nextSong(path: String) = Action {
    Ok(path |> Utils.parseSong |> songSelector.followingSong |> Utils.toJson)
  }
}
