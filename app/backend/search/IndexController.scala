package backend.search

import javax.inject.Inject

import controllers.PlayActionConverter
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.ToFunctorOps

/** Used for updating the search index from the client. */
class IndexController @Inject() (
    $ : IndexFormatter,
    ec: ExecutionContext,
    converter: PlayActionConverter,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def index() = converter.ok($.index() >| "Done")
}
