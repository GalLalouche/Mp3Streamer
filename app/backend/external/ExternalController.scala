package backend.external

import controllers.PlayActionConverter
import javax.inject.Inject
import play.api.mvc.InjectedController

class ExternalController @Inject() (
    $ : ExternalFormatter,
    converter: PlayActionConverter,
) extends InjectedController {
  def get(path: String) = converter.ok($.get(path))
  def refresh(path: String) = converter.ok($.refresh(path))
  def updateRecon(path: String) = converter.parseJson($.updateRecon(path, _))
}
