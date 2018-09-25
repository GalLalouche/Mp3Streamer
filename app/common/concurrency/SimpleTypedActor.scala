package common.concurrency

import common.rich.func.ToMoreFunctorOps

import scala.concurrent.Future

import scalaz.std.FutureInstances

/** It's a single threaded future factory basically. */
trait SimpleTypedActor[Msg, Result] extends ToMoreFunctorOps with FutureInstances {
  def !(m: => Msg): Future[Result]
}

object SimpleTypedActor {
  def apply[Msg, Result](name: String, f: Msg => Result): SimpleTypedActor[Msg, Result] =
    new SimpleTypedActorImpl(name, f)

  def unique[Msg, Result](name: String, f: Msg => Result): SimpleTypedActor[Msg, Result] =
    new UniqueSimpleTypedActorImpl(name, f)
}
