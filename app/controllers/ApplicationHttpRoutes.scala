package controllers

import java.io.File

import cats.effect.IO
import http4s.Http4sUtils
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

class ApplicationHttpRoutes {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root =>
      Http4sUtils.sendFile(req)(new File("public/html/main.html"))
    case req @ GET -> Root / "mute" =>
      Http4sUtils.sendFile(req)(new File("public/html/main.html"))
    case req @ GET -> Root / "mp3" =>
      Http4sUtils.sendFile(req)(new File("public/html/main.html"))
  }
}
