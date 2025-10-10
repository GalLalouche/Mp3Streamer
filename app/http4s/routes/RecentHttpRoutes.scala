package http4s.routes

import backend.recent.RecentFormatter
import com.google.inject.Inject
import http4s.routes.Http4sUtils.{fromFuture, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext

import cats.effect.IO

private class RecentHttpRoutes @Inject() ($ : RecentFormatter, ec: ExecutionContext) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "albums" => Ok(fromFuture($.all(10)))
    case GET -> Root / "albums" / IntVar(amount) => Ok(fromFuture($.all(amount)))
    case GET -> Root / "double" => Ok(fromFuture($.double))
    case GET -> Root / "double" / IntVar(amount) => Ok(fromFuture($.double(amount)))
    case GET -> Root / "get_last" => Ok($.getLastState)
    case GET -> Root / "update_last" => Ok(fromFuture($.updateLast()))
    case GET -> Root / "since" / dayString => Ok(fromFuture($.since(dayString)))
  }
}
