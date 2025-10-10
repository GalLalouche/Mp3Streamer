package http4s.routes

import backend.lucky.LuckyFormatter
import com.google.inject.Inject
import http4s.routes.Http4sUtils.{decodePath, fromFuture}
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.io._
import org.http4s.headers.Location

import cats.effect.IO

private class LuckyHttpRoutes @Inject() ($ : LuckyFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> "search" /: query => Ok(fromFuture($.search(decodePath(query))))
    case GET -> "redirect" /: query =>
      fromFuture($.search(decodePath(query)))
        .flatMap(url => SeeOther(Location(Uri.unsafeFromString(url))))
  }
}
