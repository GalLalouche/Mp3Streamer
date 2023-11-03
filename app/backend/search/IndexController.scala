package backend.search

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scalaz.Scalaz.ToFunctorOps

import common.rich.func.BetterFutureInstances._
import controllers.PlayActionConverter
import play.api.mvc.InjectedController

/** Used for updating the search index from the client. */
class IndexController @Inject() (
    $ : IndexFormatter,
    ec: ExecutionContext,
    converter: PlayActionConverter,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def index() = converter.ok($.index() >| "Done")
}
