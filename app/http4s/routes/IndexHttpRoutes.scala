package http4s.routes

import backend.search.IndexFormatter
import cats.effect.IO
import com.google.inject.Inject
import http4s.routes.Http4sUtils.fromFutureBlocking
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.ToFunctorOps

/** Used for updating the search index from the client. */
private class IndexHttpRoutes @Inject() ($ : IndexFormatter, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root / "index" =>
    // TODO Not sure why this blocking is needed, since the computation itself should be performed
    //  on an execution context thread...
    Ok(fromFutureBlocking($.index() >| "Done"))
  }
}
