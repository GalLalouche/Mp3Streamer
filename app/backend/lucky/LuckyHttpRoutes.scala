package backend.lucky

import javax.inject.Inject

import cats.effect.IO
import http4s.Http4sUtils.{decodePath, fromFuture}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

class LuckyHttpRoutes @Inject() ($ : LuckyFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> "search" /: query => Ok(fromFuture($.search(decodePath(query))))
    // FIXME this isn't actually working...
    case GET -> "redirect" /: query => PermanentRedirect(fromFuture($.search(decodePath(query))))
  }
}
