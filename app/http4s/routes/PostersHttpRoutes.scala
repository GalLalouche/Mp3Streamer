package http4s.routes

import com.google.inject.Inject
import musicfinder.PostersFormatter
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import cats.effect.IO
import common.rich.func.kats.ToMoreFoldableOps.toMoreFoldableOps

private class PostersHttpRoutes @Inject() ($ : PostersFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case req @ GET -> path =>
    val decoded = Http4sUtils.decodePath(path)
    $.image(decoded).mapHeadOrElse(
      Http4sUtils.sendFile(req),
      NotFound(s"<$decoded> is not a valid poster path"),
    )
  }
}
