package playlist

import controllers.{ControllerSongJsonifier, UrlPathUtils}
import javax.inject.Inject
import play.api.libs.json.JsValue

import scala.concurrent.duration.DurationInt

import common.io.JsonableSaver
import common.json.JsonReadable
import common.json.ToJsonableOps._
import common.rich.RichT._
import common.json.RichJson._

private class PlaylistFormatter @Inject()(
    saver: JsonableSaver,
    urlPathUtils: UrlPathUtils,
    songJsonifier: ControllerSongJsonifier,
) {
  import songJsonifier.songJsonable

  def getQueue: JsValue = saver.loadObject[PlaylistQueue].songs.jsonify
  private implicit val parseQueue: JsonReadable[PlaylistQueue] =
    _.parse[Seq[String]] map urlPathUtils.parseSong mapTo PlaylistQueue.apply
  def setQueue(json: JsValue): Unit = {
    val queue = json.parse[PlaylistQueue]
    saver saveObject queue
  }

  def getState: JsValue = saver.loadObject[PlaylistState].jsonify
  // TODO (again) remove code duplication with JsonReadable[PlaylistState]
  private implicit val parseState: JsonReadable[PlaylistState] = json => {
    val songs = json.value("songs").parse[Seq[String]] map urlPathUtils.parseSong
    val duration: Double = json double "duration"
    val currentIndex: Int = json int "currentIndex"
    PlaylistState(songs, currentIndex, duration.toInt.seconds)
  }
  def setState(json: JsValue): Unit = saver saveObject json.parse[PlaylistState]
}

