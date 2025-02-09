package controllers

import javax.inject.Inject

import cats.effect.IO
import http4s.Http4sUtils.{decodePath, fromFuture, ShouldEncodeMp3}
import org.http4s.{Header, Headers, HttpRoutes, MediaType, Status}
import org.http4s.dsl.io._
import org.http4s.headers.{`Content-Length`, `Content-Type`}
import org.http4s.implicits.http4sSelectSyntaxOne
import org.typelevel.ci.CIString

import common.rich.collections.RichTraversableOnce._

class StreamHttpRoutes @Inject() ($ : StreamerFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case req @ GET -> "download" /: path =>
    for {
      streamResult <- fromFuture(
        $(
          decodePath(path),
          req.headers.get(CIString("Range")).map(_.toList.single.value),
          ShouldEncodeMp3,
        ),
      )
      // TODO creating an OK just to replace it with another status is kinda silly.
      contentLength = `Content-Length`(streamResult.contentLength).toRaw1
      result <- Ok(
        fs2.io.readInputStream(IO.pure(streamResult.inputStream), 4096),
        Headers(streamResult.headers.view.map(Header.ToRaw.keyValuesToRaw).toVector: _*)
          .put(contentLength),
      )
    } yield result
      .withContentType(`Content-Type`(MediaType.parse(streamResult.mimeType).right.get))
      .withStatus(Status.fromInt(streamResult.status).right.get)
  }
}
