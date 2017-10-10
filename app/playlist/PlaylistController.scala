package playlist

import common.RichJson._
import common.io.JsonableSaver
import common.rich.RichT._
import controllers.{ControllerUtils, LegacyController}
import models.Song
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc.{Action, Controller}
import playlist.PlaylistQueue._
import playlist.PlaylistState.PlaylistStateJsonable

import scala.concurrent.duration.DurationInt

object PlaylistController extends LegacyController {
  private val saver = new JsonableSaver()(ControllerUtils.config.rootDirectory) // since implicit importing is auto-removed

  private def arrayOfPathsToSong(a: JsArray): Seq[Song] = a.value.map(_.as[String]).map(ControllerUtils.parseSong)

  import models.ModelJsonable._
  def getQueue = Action {
    Ok(saver.loadObject[PlaylistQueue].songs map ControllerUtils.toJson mapTo JsArray.apply)
  }
  def setQueue() = Action { request =>
    val playlist = request.body.asJson.get.as[JsArray] |> arrayOfPathsToSong |> PlaylistQueue.apply
    saver save playlist
    Created.withHeaders("Location" -> "playlist/queue")
  }

  private def toJson(state: PlaylistState): JsObject = Json.obj(
    "songs" -> JsArray(state.songs.map(ControllerUtils.toJson)),
    "index" -> state.currentIndex,
    "duration" -> state.currentDuration.toSeconds
  )
  def getState = Action {
    Ok(saver.loadObject[PlaylistState] |> toJson)
  }
  def setState() = Action {request =>
    val json = request.body.asJson.get
    val songs = json array "songs" mapTo arrayOfPathsToSong
    val duration: Double = json double "duration"
    val index: Int = json int "index"
    saver save PlaylistState(songs, index, duration.toInt.seconds)
    Created.withHeaders("Location" -> "playlist/state")
  }
}
