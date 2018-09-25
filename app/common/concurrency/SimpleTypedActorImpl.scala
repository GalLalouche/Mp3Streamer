package common.concurrency

import common.rich.func.ToMoreFunctorOps

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances

/** It's a single threaded future factory basically. */
private class SimpleTypedActorImpl[Msg, Result](name: String, f: Msg => Result)
    extends SimpleTypedActor[Msg, Result]
        with ToMoreFunctorOps with FutureInstances {
  private implicit val service: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  def !(m: => Msg): Future[Result] = Future(f(m))
}
