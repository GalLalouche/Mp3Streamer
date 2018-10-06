package backend.external

import javax.inject.Inject
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.{Action, AnyContent, InjectedController}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class ExternalController @Inject()(
    ec: ExecutionContext,
    $: ExternalFormatter,
) extends InjectedController
    with ToBindOps with FutureInstances {
  private implicit val iec: ExecutionContext = ec

  private def ok(f: Future[JsValue]): Action[AnyContent] = Action.async {f.map(Ok(_))}

  def get(path: String) = ok($.get(path))
  def refresh(path: String) = ok($.refresh(path))
  def updateRecon(path: String) = Action.async {request =>
    val json = request.body.asJson.get.asInstanceOf[JsObject]
    $.updateRecon(path, json).map(Ok(_))
  }
}
