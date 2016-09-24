package controllers

import common.RichJson._
import common.io.JsonableSaver
import common.rich.RichT._
import models.Song
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import playlist.PlaylistQueue._
import playlist.{PlaylistQueue, PlaylistState}
import playlist.PlaylistState.PlaylistStateJsonable

import scala.concurrent.duration.DurationInt

object Playlist extends Controller {
  private implicit val c = PlayConfig
  import c._ // I have no idea why it doesn't require it
  private val saver = new JsonableSaver
  private def getStringFromBody(a: AnyContent): String = a.asFormUrlEncoded.get.keys.head
  private def arrayOfPathsToSong(a: JsArray): Seq[Song] = a.value.map(_.as[String]).map(Utils.parseSong)
  def getQueue = Action {
    Ok(saver.loadObject[PlaylistQueue].songs map Player.toJson mapTo JsArray.apply)
  }
  def setQueue = Action { request =>
    val playlist = request.body
        .mapTo(_ |> getStringFromBody |> Json.parse)
        .mapTo(_.as[JsArray] |> arrayOfPathsToSong |> PlaylistQueue.apply)
    saver save playlist
    Created.withHeaders("Location" -> "playlist/queue")
  }
  def getState = Action {
    Ok(saver.loadObject[PlaylistState] |> PlaylistStateJsonable.jsonify)
  }
  def setState = Action { request =>
    val json = request.body |> getStringFromBody |> Json.parse
    val songs = json array "songs" mapTo arrayOfPathsToSong
    val duration: Double = json / "duration"
    val index: Int = json / "index"
    saver save PlaylistState(songs, index, duration.toInt.seconds)
    Created.withHeaders("Location" -> "playlist/state")
  }
}
