package common.concurrency.actor

import scala.concurrent.Future

/** It's a single threaded future factory basically. */
private class ActorImpl[Msg, Result](name: String, f: ActorFunction[Msg, Result])
    extends ActorTemplate[Msg, Result](name) {
  // Delegate both handles failures during run and ensure single message processing.
  def !(m: => Msg): Future[Result] = Future.delegate(f(this, m))
}
