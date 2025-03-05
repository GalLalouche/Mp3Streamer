package http4s.routes

import backend.external.ExternalFormatter
import cats.effect.IO
import com.google.inject.Inject
import http4s.routes.Http4sUtils.{decodePath, fromFuture, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

private[http4s] class ExternalHttpRoutes @Inject() ($ : ExternalFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> "refresh" /: "artist" /: path => Ok(fromFuture($.refreshArtist(decodePath(path))))
    case GET -> "refresh" /: "album" /: path => Ok(fromFuture($.refreshAlbum(decodePath(path))))
    case req @ POST -> "recons" /: path =>
      Ok(Http4sUtils.parseJson(req, json => $.updateRecon(decodePath(path), json)))
    case GET -> path => Ok(fromFuture($.get(decodePath(path))))
  }
}
