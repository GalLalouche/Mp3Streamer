package http4s.routes

import backend.last_albums.LastAlbumsFormatter
import com.google.inject.Inject
import http4s.routes.Http4sUtils.{fromFuture, fromFutureIO, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext

import cats.effect.IO

private class LastAlbumsHttpRoutes @Inject() ($ : LastAlbumsFormatter, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case POST -> Root / "dequeue" =>
      fromFutureIO($.dequeueNextAlbum().cata(NotFound("No next album"), Ok(_)))
    case POST -> Root / "update" => Ok(fromFuture($.updateLast()))
    case GET -> Root => Ok($.getLast)
  }
}
