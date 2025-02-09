package backend.search

import javax.inject.Inject

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.ToFunctorOps

/** Used for updating the search index from the client. */
class IndexHttpRoutes @Inject() ($ : IndexFormatter, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec
  import http4s.Http4sUtils._
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root / "index" =>
    Ok(fromFuture($.index() >| "Done"))
  }
}
