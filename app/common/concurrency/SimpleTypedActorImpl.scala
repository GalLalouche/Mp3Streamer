package common.concurrency

import scala.concurrent.{ExecutionContext, Future}

/** It's a single threaded future factory basically. */
private class SimpleTypedActorImpl[Msg, Result](name: String, f: Msg => Result)
    extends SimpleTypedActor[Msg, Result] {
  private implicit val service: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  def !(m: => Msg): Future[Result] = Future(f(m))
}
