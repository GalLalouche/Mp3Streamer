package backend.external

import controllers.FormatterUtils
import javax.inject.Inject
import play.api.libs.json.JsObject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class ExternalController @Inject()(
    ec: ExecutionContext,
    $: ExternalFormatter,
    formatterUtils: FormatterUtils,
) extends InjectedController
    with ToBindOps with FutureInstances {
  private implicit val iec: ExecutionContext = ec

  def get(path: String) = formatterUtils.ok($.get(path))
  def refresh(path: String) = formatterUtils.ok($.refresh(path))
  def updateRecon(path: String) =
    formatterUtils.parse(_.body.asJson.get.asInstanceOf[JsObject])($.updateRecon(path, _))
}
