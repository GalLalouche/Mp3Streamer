package controllers

import common.io.JsonableSaver
import common.RichJson._
import common.rich.RichT._
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import playlist.PlaylistQueue
import playlist.PlaylistQueue._
import models.Song
import playlist.PlaylistClone
import playlist.PlaylistClone.PlaylistCloneJsonable
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
    Created.withHeaders("Location" -> "playlist")
  }
  def getClone = Action {
    Ok(saver.loadObject[PlaylistClone] |> PlaylistCloneJsonable.jsonify)
  }
  def setClone = Action { request =>
    val json = request.body |> getStringFromBody |> Json.parse
    val songs = json array "songs" mapTo arrayOfPathsToSong
    val duration: Int = json / "duration"
    val index: Int = json / "index"
    saver save PlaylistClone(songs, index, duration.seconds)
    Created.withHeaders("Location" -> ???)
  }
}
