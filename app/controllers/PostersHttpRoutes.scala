package controllers

import javax.inject.Inject

import http4s.{Http4sUtils, RouteProvider}
import http4s.RouteProvider.Routes
import org.http4s.dsl.io._

import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.Scalaz.optionInstance

class PostersHttpRoutes @Inject() ($ : PostersFormatter) extends RouteProvider {
  protected override val routes: Routes = { case req @ GET -> path =>
    val decoded = Http4sUtils.decodePath(path)
    $.image(decoded).mapHeadOrElse(
      Http4sUtils.sendFile(req),
      NotFound(s"<$decoded> is not a valid poster path"),
    )
  }
}
