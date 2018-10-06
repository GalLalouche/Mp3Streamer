package backend.external

import controllers.FormatterUtils
import javax.inject.Inject
import play.api.mvc.InjectedController

class ExternalController @Inject()(
    $: ExternalFormatter,
    formatterUtils: FormatterUtils,
) extends InjectedController {
  def get(path: String) = formatterUtils.ok($.get(path))
  def refresh(path: String) = formatterUtils.ok($.refresh(path))
  def updateRecon(path: String) = formatterUtils.parseJson($.updateRecon(path, _))
}
