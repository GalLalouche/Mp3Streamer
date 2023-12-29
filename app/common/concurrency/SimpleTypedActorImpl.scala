package common.concurrency

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.syntax.functor.ToFunctorOps

/** It's a single threaded future factory basically. */
private class SimpleTypedActorImpl[Msg, +Result](name: String, f: Msg => Result)
    extends SimpleTypedActor[Msg, Result] {
  private implicit val ec: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  def !(m: => Msg): Future[Result] = Future(f(m))

  def void: SimpleActor[Msg] = SimpleTypedActorImpl.this.!(_).void
}
