package playlist

import common.RichJson._
import common.io.JsonableSaver
import common.json.{JsonReadable, ToJsonableOps}
import common.rich.RichT._
import controllers.ControllerUtils.songJsonable
import controllers.UrlPathUtils
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.duration.DurationInt

class PlaylistController @Inject()(saver: JsonableSaver) extends InjectedController
    with ToJsonableOps {

  def getQueue = Action {
    Ok(saver.loadObject[PlaylistQueue].songs.jsonify)
  }
  private implicit val parseQueue: JsonReadable[PlaylistQueue] =
    _.parse[Seq[String]] map UrlPathUtils.parseSong mapTo PlaylistQueue.apply
  def setQueue() = Action {request =>
    val queue = request.body.asJson.get.parse[PlaylistQueue]
    saver save queue
    Created.withHeaders("Location" -> "playlist/queue")
  }

  // TODO (again) remove code duplication with JsonReadable[PlaylistState]
  private implicit val parseState: JsonReadable[PlaylistState] = json => {
    val songs = json.value("songs").parse[Seq[String]] map UrlPathUtils.parseSong
    val duration: Double = json double "duration"
    val index: Int = json int "index"
    PlaylistState(songs, index, duration.toInt.seconds)
  }
  def getState = Action {
    Ok(saver.loadObject[PlaylistState].jsonify)
  }
  def setState() = Action {request =>
    val state = request.body.asJson.get.parse[PlaylistState]
    saver save state
    Created.withHeaders("Location" -> "playlist/state")
  }
}
