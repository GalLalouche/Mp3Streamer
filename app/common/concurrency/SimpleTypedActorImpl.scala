package common.concurrency

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.syntax.functor.ToFunctorOps

private class SimpleTypedActorImpl[Msg, +Result](name: String, f: Msg => Result)
    extends SimpleTypedActor[Msg, Result] {
  protected implicit val ec: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  def !(m: => Msg): Future[Result] = Future(f(m))

  def void: SimpleActor[Msg] = SimpleTypedActorImpl.this.!(_).void
}
