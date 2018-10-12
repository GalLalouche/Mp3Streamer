package playlist

import controllers.FormatterUtils
import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc.InjectedController

class PlaylistController @Inject()($: PlaylistFormatter, formatterUtils: FormatterUtils)
    extends InjectedController {

  def getQueue = formatterUtils.ok($.getQueue)
  private def setAndReturnLocation(setFromJson: JsValue => Unit, path: String) =
    formatterUtils.parseJson(j => {
      setFromJson(j)
      Created.withHeaders("Location" -> ("playlist/" + path))
    })
  def setQueue() = setAndReturnLocation($.setQueue, "queue")

  def getState = formatterUtils.ok($.getState)
  def setState() = setAndReturnLocation($.setState, "state")
}
