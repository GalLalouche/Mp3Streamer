package http4s.routes

import javax.inject.Inject

import cats.effect.IO
import controllers.{StreamerFormatter, StreamResult}
import http4s.routes.Http4sUtils.{decodePath, fromFuture, shouldEncodeMp3}
import org.http4s.{Header, Headers, HttpRoutes, MediaType, Response, Status}
import org.http4s.dsl.io._
import org.http4s.headers.{`Content-Length`, `Content-Type`}
import org.http4s.implicits.http4sSelectSyntaxOne
import org.typelevel.ci.CIString

import common.rich.collections.RichTraversableOnce._

private class StreamHttpRoutes @Inject() ($ : StreamerFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case req @ GET -> "download" /: path =>
    val range = req.headers.get(CIString("Range")).map(_.toList.single.value)
    fromFuture($(decodePath(path), range, shouldEncodeMp3(req))).map(toResponse)
  }

  private def toResponse(sr: StreamResult): Response[IO] = Response[IO](
    body = fs2.io.readInputStream(IO.pure(sr.inputStream), 4096),
    status = Status.fromInt(sr.status).right.get,
    headers = Headers(sr.headers.view.map(Header.ToRaw.keyValuesToRaw).toVector: _*)
      .put(`Content-Length`(sr.contentLength).toRaw1),
  ).withContentType(`Content-Type`(MediaType.parse(sr.mimeType).right.get))
}
