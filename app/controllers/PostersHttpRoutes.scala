package controllers

import javax.inject.Inject

import cats.effect.IO
import http4s.Http4sUtils
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.Scalaz.optionInstance

class PostersHttpRoutes @Inject() ($ : PostersFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case req @ GET -> path =>
    val decoded = Http4sUtils.decodePath(path)
    $.image(decoded).mapHeadOrElse(
      Http4sUtils.sendFile(req),
      NotFound(s"<$decoded> is not a valid poster path"),
    )
  }
}
