package common.concurrency.actor

import scala.concurrent.Future

private class SimpleTypedActorImpl[Msg, +Result](
    name: String,
    f: (Msg, SimpleTypedActor[Msg, Result]) => Result,
) extends SimpleTypedActorTemplate[Msg, Result](name) {
  def this(name: String, f: Msg => Result) =
    this(name, (m: Msg, _: SimpleTypedActor[Msg, Result]) => f(m))
  override def !(m: => Msg): Future[Result] = Future(f(m, this))
}
