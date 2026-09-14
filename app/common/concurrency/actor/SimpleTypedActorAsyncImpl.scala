package common.concurrency.actor

import scala.concurrent.Future
import scala.util.Try

import common.rich.primitives.RichTry.richTry

/** It's a single threaded future factory basically. */
private class SimpleTypedActorAsyncImpl[Msg, Result](name: String, f: Msg => Future[Result])
    extends SimpleTypedActorTemplate[Msg, Result](name) {
  def !(m: => Msg): Future[Result] = Try(f(m)).getOrElseF(Future.failed)
}
