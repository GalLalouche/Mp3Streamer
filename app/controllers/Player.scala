package controllers

import common.Debug
import common.io.IODirectory
import common.json.ToJsonableOps
import common.rich.RichT._
import common.rich.primitives.RichEither._
import controllers.ControllerUtils.songJsonable
import models._
import play.api.libs.json.JsValue
import play.api.mvc._
import songs.{SongGroup, SongGroups, SongSelector}

/** Handles fetch requests of JSON information, and listens to directory changes. */
object Player extends LegacyController with ToJsonableOps with Debug {
  private val songGroups: Map[Song, SongGroup] = {
    SongGroups.fromGroups(new SongGroups().load)
  }
  private var songSelector: SongSelector = _
  def update(): Unit = {
    songSelector = SongSelector.create
  }
  //TODO hide this, shouldn't be a part of the controller
  update()

  private def toJson(e: Either[Song, SongGroup]): JsValue = e.resolve(_.jsonify, _.songs.jsonify)

  private def group(s: Song): Either[Song, SongGroup] = songGroups get s toRight s

  def randomSong = Action {
    Ok(songSelector.randomSong |> group |> toJson)
  }

  private def songsInAlbum(path: String): Seq[Song] =
    ControllerUtils.parseFile(path) |> IODirectory.apply |> Album.apply |> Album.songs.get
  def album(path: String) = Action {
    Ok(songsInAlbum(path).jsonify)
  }
  def discNumber(path: String, requestedDiscNumber: String) = Action {
    val songsWithDiscNumber =
      songsInAlbum(path).filter(_.discNumber.exists(requestedDiscNumber ==)).ensuring(_.nonEmpty)
    Ok(songsWithDiscNumber.jsonify)
  }

  def song(path: String) = Action {
    Ok(path.parseJsonable[Song] |> group |> toJson)
  }

  def nextSong(path: String) = Action {
    val song = path.parseJsonable[Song]
    val nextSong: Option[Song] = songSelector followingSong song
    Ok(nextSong.get.jsonify)
  }
}
