package http4s.routes

import java.io.File
import javax.inject.Inject

import cats.effect.IO
import http4s.routes.AssetHttpRoutes.asset
import http4s.routes.Http4sUtils.decodePath
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.io._

// Since 2.6 ruined their own assets controller :\
private class AssetHttpRoutes @Inject() {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> "assets" /: path => asset(req, decodePath(path))
    case req @ GET -> "js" /: path => asset(req, "javascripts\\" + decodePath(path))
    case req @ GET -> "ts" /: path =>
      Http4sUtils.sendFile(req)(new File("target/web/public/main/typescripts/" + decodePath(path)))
  }
}

private object AssetHttpRoutes {
  private def asset(req: Request[IO], path: String): IO[Response[IO]] =
    Http4sUtils.sendFile(req)(new File("public\\" + path))
}
