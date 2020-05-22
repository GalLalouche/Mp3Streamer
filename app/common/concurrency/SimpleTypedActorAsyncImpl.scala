package common.concurrency

import scala.concurrent.{ExecutionContext, Future, Promise}

import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._

/** It's a single threaded future factory basically. */
private class SimpleTypedActorAsyncImpl[Msg, Result](name: String, f: Msg => Future[Result])
    extends SimpleTypedActor[Msg, Result] {
  private implicit val ec: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  def !(m: => Msg): Future[Result] = {
    val promise = Promise[Result]
    ec.execute(() => f(m).onComplete(promise.complete))
    promise.future
  }
  def void: SimpleActor[Msg] = SimpleTypedActorAsyncImpl.this.!(_).void
}
