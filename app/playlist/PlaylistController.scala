package playlist

import javax.inject.Inject

import controllers.PlayActionConverter
import play.api.mvc.{Action, AnyContent, InjectedController}

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.Scalaz.{optionInstance, ToFunctorOps}

class PlaylistController @Inject() (
    $ : PlaylistFormatter,
    converter: PlayActionConverter,
    ec: ExecutionContext,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def set(id: String): Action[AnyContent] = converter.parseJson($.set(id, _).>|(Created(id)))
  def getIds: Action[AnyContent] = converter.ok($.getIds)
  def get(id: String): Action[AnyContent] =
    Action.async($.get(id).map(_.mapHeadOrElse(Ok(_), NotFound)))
  def remove(id: String): Action[AnyContent] = converter.ok($.remove(id).map(_.toString))
}
