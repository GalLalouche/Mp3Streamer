package backend.search

import javax.inject.Inject

import cats.effect.IO
import http4s.Http4sUtils.{decodePath, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

class SearchHttpRoutes @Inject() ($ : SearchFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> path =>
    Ok($.search(decodePath(path)))
  }
}
