package backend.external

import javax.inject.Inject

import cats.effect.IO
import http4s.Http4sUtils
import http4s.Http4sUtils.{decodePath, fromFuture, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

class ExternalHttpRoutes @Inject() ($ : ExternalFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> "refresh" /: "artist" /: path => Ok(fromFuture($.refreshArtist(decodePath(path))))
    case GET -> "refresh" /: "album" /: path => Ok(fromFuture($.refreshAlbum(decodePath(path))))
    case req @ POST -> "recons" /: path =>
      Ok(Http4sUtils.parseJson(req, json => $.updateRecon(decodePath(path), json)))
    case GET -> path => Ok(fromFuture($.get(decodePath(path))))
  }
}
