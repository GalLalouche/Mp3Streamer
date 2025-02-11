package playlist

import javax.inject.Inject

import http4s.Http4sUtils.{fromFuture, fromFutureIO, jsonEncoder, parseJson}
import http4s.RouteProvider
import http4s.RouteProvider.Routes
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.Scalaz.{optionInstance, ToFunctorOps}

class PlaylistHttpRoutes @Inject() (
    $ : PlaylistFormatter,
    ec: ExecutionContext,
) extends RouteProvider {
  private implicit val iec: ExecutionContext = ec
  override val routes: Routes = {
    case GET -> Root => Ok(fromFuture($.getIds))
    case GET -> Root / id => fromFutureIO($.get(id).map(_.mapHeadOrElse(f => Ok(f), NotFound(""))))
    case req @ PUT -> Root / id => Created(parseJson(req, $.set(id, _) >| id))
    case DELETE -> Root / id => Ok(fromFuture($.remove(id).map(_.toString)))
  }
}
