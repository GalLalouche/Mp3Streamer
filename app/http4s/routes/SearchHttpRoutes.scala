package http4s.routes

import backend.search.SearchFormatter
import cats.effect.IO
import com.google.inject.Inject
import http4s.routes.Http4sUtils.{decodePath, fromFuture, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

private class SearchHttpRoutes @Inject() ($ : SearchFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> path =>
    Ok(fromFuture($.search(decodePath(path))))
  }
}
