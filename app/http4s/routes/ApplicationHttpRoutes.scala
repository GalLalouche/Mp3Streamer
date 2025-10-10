package http4s.routes

import java.io.File

import http4s.routes.Http4sUtils.sendFile
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import cats.effect.IO

private class ApplicationHttpRoutes {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root => sendFile(req)(new File("public/html/main.html"))
    case req @ GET -> Root / "mute" => sendFile(req)(new File("public/html/main.html"))
    case req @ GET -> Root / "mp3" => sendFile(req)(new File("public/html/main.html"))
  }
}
