package playlist

import javax.inject.Inject

import controllers.PlayActionConverter
import play.api.libs.json.JsValue
import play.api.mvc.InjectedController

class PlaylistController @Inject() ($ : PlaylistFormatter, converter: PlayActionConverter)
    extends InjectedController {
  def getQueue = converter.ok($.getQueue)
  private def setAndReturnLocation(setFromJson: JsValue => Unit, path: String) =
    converter.parseJson { j =>
      setFromJson(j)
      Created.withHeaders("Location" -> ("playlist/" + path))
    }
  def setQueue() = setAndReturnLocation($.setQueue, "queue")

  def getState = converter.ok($.getState)
  def setState() = setAndReturnLocation($.setState, "state")
}
