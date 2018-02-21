package playlist

import common.io.JsonableSaver
import common.json.ToJsonableOps
import controllers.ControllerUtils.songJsonable
import controllers.LegacyController
import play.api.mvc.Action

object PlaylistController extends LegacyController
    with ToJsonableOps {
  private val saver = new JsonableSaver()

  def getQueue = Action {
    Ok(saver.loadObject[PlaylistQueue].songs.jsonify)
  }
  def setQueue() = Action { request =>
    val playlistQueue = request.body.asJson.get.parse[PlaylistQueue]
    saver save playlistQueue
    Created.withHeaders("Location" -> "playlist/queue")
  }

  def getState = Action {
    Ok(saver.loadObject[PlaylistState].jsonify)
  }
  def setState() = Action { request =>
    val playlistState = request.body.asJson.get.parse[PlaylistState]
    saver save playlistState
    Created.withHeaders("Location" -> "playlist/state")
  }
}
