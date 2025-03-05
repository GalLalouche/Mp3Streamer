package http4s.routes

import cats.effect.IO
import com.google.inject.Inject
import http4s.routes.Http4sUtils.{fromFuture, fromFutureIO, jsonEncoder, parseJson}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import playlist.PlaylistFormatter

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.Scalaz.{optionInstance, ToFunctorOps}

private class PlaylistHttpRoutes @Inject() ($ : PlaylistFormatter, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => Ok(fromFuture($.getIds))
    case GET -> Root / id => fromFutureIO($.get(id).map(_.mapHeadOrElse(f => Ok(f), NotFound(""))))
    case req @ PUT -> Root / id => Created(parseJson(req, $.set(id, _) >| id))
    case DELETE -> Root / id => Ok(fromFuture($.remove(id).map(_.toString)))
  }
}
