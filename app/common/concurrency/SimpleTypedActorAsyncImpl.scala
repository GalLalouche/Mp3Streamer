package common.concurrency

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

import cats.implicits.toFunctorOps

import common.rich.RichFuture

/** It's a single threaded future factory basically. */
private class SimpleTypedActorAsyncImpl[Msg, Result](name: String, f: Msg => Future[Result])
    extends SimpleTypedActor[Msg, Result] {
  private implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)
  def !(m: => Msg): Future[Result] =
    RichFuture.fromTryCallback(c =>
      ec.execute(() =>
        try
          f(m).onComplete(c)
        catch {
          case e: Throwable => c(Failure(e))
        },
      ),
    )
  def void: SimpleActor[Msg] = SimpleTypedActorAsyncImpl.this.!(_).void
}
