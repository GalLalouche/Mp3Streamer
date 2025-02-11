package http4s

import cats.effect.IO
import http4s.RouteProvider.Routes
import org.http4s.{HttpRoutes, Request, Response}

trait RouteProvider {
  protected def routes: Routes
  def httpRoutes: HttpRoutes[IO] = HttpRoutes.of(routes)
}
object RouteProvider {
  // Nicer type for override function explicit type
  type Routes = PartialFunction[Request[IO], IO[Response[IO]]]
}
