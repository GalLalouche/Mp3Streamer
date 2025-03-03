package http4s.routes

import java.io.File
import javax.inject.Inject

import cats.effect.IO
import http4s.routes.AssetHttpRoutes.asset
import http4s.routes.Http4sUtils.decodePath
import org.http4s.{HttpRoutes, MediaType, Request, Response}
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`

// Since 2.6 ruined their own assets controller :\
private class AssetHttpRoutes @Inject() {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> "assets" /: path => asset(req, decodePath(path))
    case req @ GET -> "js" /: path => asset(req, "javascripts\\" + decodePath(path))
    case req @ GET -> "ts" /: path =>
      val p = decodePath(path)
      val pathPrefix = if (p.endsWith("ts")) "public/" else "target/web/public/main/"
      val file = new File(s"$pathPrefix/typescripts/$p")
      Http4sUtils
        .sendFileOrNotFound(req, file)
        .map(_.withContentType(`Content-Type`(MediaType.application.javascript)))
  }
}

private object AssetHttpRoutes {
  private def asset(req: Request[IO], path: String): IO[Response[IO]] =
    Http4sUtils.sendFileOrNotFound(req, new File("public\\" + path))
}
