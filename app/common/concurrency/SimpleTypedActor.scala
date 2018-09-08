package common.concurrency

import common.rich.func.ToMoreFunctorOps

import scala.concurrent.Future

import scalaz.std.FutureInstances

/** It's a single threaded future factory basically. */
trait SimpleTypedActor[Msg, Result] extends ToMoreFunctorOps with FutureInstances {
  def !(m: => Msg): Future[Result]
}

object SimpleTypedActor {
  def apply[Msg, Result](f: Msg => Result, unique: Boolean = false): SimpleTypedActor[Msg, Result] =
    new SimpleTypedActorImpl(f, unique)
}
