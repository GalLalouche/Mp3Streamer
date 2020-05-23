package common.concurrency

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._

import common.rich.RichFuture

/** It's a single threaded future factory basically. */
private class SimpleTypedActorAsyncImpl[Msg, Result](name: String, f: Msg => Future[Result])
    extends SimpleTypedActor[Msg, Result] {
  private implicit val ec: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  def !(m: => Msg): Future[Result] =
    RichFuture.fromTryCallback(c => ec.execute(() => f(m).onComplete(c)))
  def void: SimpleActor[Msg] = SimpleTypedActorAsyncImpl.this.!(_).void
}
