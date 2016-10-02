package controllers

import common.RichJson._
import common.io.JsonableSaver
import common.rich.RichT._
import models.Song
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import playlist.PlaylistQueue._
import playlist.PlaylistState.PlaylistStateJsonable
import playlist.{PlaylistQueue, PlaylistState}

import scala.concurrent.duration.DurationInt

object Playlist extends Controller {
  private val saver = new JsonableSaver()(PlayConfig.rootDirectory) // since implicit importing is auto-removed

  private def getStringFromBody(a: AnyContent): String = a.asFormUrlEncoded.get.keys.head
  private def arrayOfPathsToSong(a: JsArray): Seq[Song] = a.value.map(_.as[String]).map(Utils.parseSong)

  def getQueue = Action {
    Ok(saver.loadObject[PlaylistQueue].songs map Utils.toJson mapTo JsArray.apply)
  }
  def setQueue() = Action { request =>
    val playlist = request.body
        .mapTo(_ |> getStringFromBody |> Json.parse)
        .mapTo(_.as[JsArray] |> arrayOfPathsToSong |> PlaylistQueue.apply)
    saver save playlist
    Created.withHeaders("Location" -> "playlist/queue")
  }

  private def toJson(state: PlaylistState): JsObject = Json.obj(
    "songs" -> JsArray(state.songs.map(Utils.toJson)),
    "index" -> state.currentIndex,
    "duration" -> state.currentDuration.toSeconds
  )
  def getState = Action {
    Ok(saver.loadObject[PlaylistState] |> toJson)
  }
  def setState() = Action { request =>
    val json = request.body |> getStringFromBody |> Json.parse
    val songs = json array "songs" mapTo arrayOfPathsToSong
    val duration: Double = json / "duration"
    val index: Int = json / "index"
    saver save PlaylistState(songs, index, duration.toInt.seconds)
    Created.withHeaders("Location" -> "playlist/state")
  }
}
