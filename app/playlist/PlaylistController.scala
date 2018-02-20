package playlist

import common.Jsonable
import common.Jsonable.ToJsonableOps
import common.RichJson._
import common.io.FormatSaver
import common.rich.RichT._
import controllers.{ControllerUtils, LegacyController}
import models.Song
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.mvc.Action
import playlist.PlaylistQueue._

import scala.concurrent.duration.DurationInt

object PlaylistController extends LegacyController
    with ToJsonableOps {
  private val saver = new FormatSaver()

  private def arrayOfPathsToSong(a: JsArray): Seq[Song] = a.value.map(_.as[String]).map(ControllerUtils.parseSong)

  import models.ModelJsonable._

  def getQueue = Action {
    Ok(saver.loadObject[PlaylistQueue].songs.map(ControllerUtils.toJson).jsonify)
  }
  def setQueue() = Action {request =>
    val playlist = request.body.asJson.get.as[JsArray] |> arrayOfPathsToSong |> PlaylistQueue.apply
    saver save playlist
    Created.withHeaders("Location" -> "playlist/queue")
  }

  // Special handling of song jsonify and parsing
  // TODO make PlaylistState receive an implicit SongJsonifier
  private implicit object PlaylistStateJsonable extends Jsonable[PlaylistState] {
    override def jsonify(state: PlaylistState) = Json.obj(
      "songs" -> JsArray(state.songs.map(ControllerUtils.toJson)),
      "index" -> state.currentIndex,
      "duration" -> state.currentDuration.toSeconds
    )
    override def parse(json: JsValue) = {
      val songs = json array "songs" mapTo arrayOfPathsToSong
      val duration: Double = json double "duration"
      val index: Int = json int "index"
      PlaylistState(songs, index, duration.toInt.seconds)
    }
  }

  def getState = Action {
    Ok(saver.loadObject[PlaylistState].jsonify)
  }
  def setState() = Action {request =>
    val json = request.body.asJson.get
    saver save json.parse[PlaylistState]
    Created.withHeaders("Location" -> "playlist/state")
  }
}
