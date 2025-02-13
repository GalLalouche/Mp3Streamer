package http4s.routes

import javax.inject.Inject

import backend.recent.RecentFormatter
import cats.effect.IO
import http4s.routes.Http4sUtils.{fromFuture, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext

private class RecentHttpRoutes @Inject() ($ : RecentFormatter, ec: ExecutionContext) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "albums" => Ok(fromFuture($.all(0)))
    case GET -> Root / "albums" / IntVar(amount) => Ok(fromFuture($.all(amount)))
    case GET -> Root / "double" => Ok(fromFuture($.double(0)))
    case GET -> Root / "double" / IntVar(amount) => Ok(fromFuture($.double(amount)))
    case GET -> Root / "last" => Ok(fromFuture($.last))
    case GET -> Root / "since" / dayString => Ok(fromFuture($.since(dayString)))
  }
}
