package controllers

import common.Debug
import common.io.IODirectory
import common.rich.RichT._
import common.rich.primitives.RichEither._
import models._
import play.api.libs.json.{JsArray, JsValue}
import play.api.mvc._
import backend.search.ModelJsonable._
import songs.{SongGroup, SongGroups, SongSelector}

/** Handles fetch requests of JSON information, and listens to directory changes. */
object Player extends Controller with Debug {
  private implicit val c = Utils.config
  import c._
  private val songGroups: Map[Song, SongGroup] = {
    SongGroups.fromGroups(new SongGroups().load)
  }
  private var songSelector: SongSelector = _
  def update(): Unit = {
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

  private def songsInAlbum(path: String): Seq[Song] =
    Utils.parseFile(path) |> IODirectory.apply |> Album.apply |> Album.songs.get
  private def songsAsJsArray(ss: Seq[Song]) =
    Ok(ss.map(Utils.toJson) |> JsArray)
  def album(path: String) = Action {
    songsInAlbum(path) |> songsAsJsArray
  }
  def discNumber(path: String, discNumber: String) = Action {
    songsInAlbum(path).filter(_.discNumber.exists(discNumber ==)).ensuring(_.nonEmpty) |> songsAsJsArray
  }

  def song(path: String) = Action {
    Ok(path |> Utils.parseSong |> group |> toJson)
  }

  def nextSong(path: String) = Action {
    val song = path |> Utils.parseSong
    val nextSong: Option[Song] = songSelector followingSong song
    Ok(nextSong.get |> Utils.toJson)
  }
}
