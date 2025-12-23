package common.concurrency

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import cats.implicits.toFunctorOps

import common.rich.primitives.RichTry.richTry

/** It's a single threaded future factory basically. */
private class SimpleTypedActorAsyncImpl[Msg, Result](name: String, f: Msg => Future[Result])
    extends SimpleTypedActor[Msg, Result] {
  private implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)
  def !(m: => Msg): Future[Result] = Try(f(m)).getOrElseF(Future.failed)
  def void: SimpleActor[Msg] = SimpleTypedActorAsyncImpl.this.!(_).void
}
