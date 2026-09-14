package common.concurrency.actor

import scala.concurrent.Future

private class UniqueSimpleTypedActorImpl[Msg, Result](
    name: String,
    f: Msg => Result,
) extends SimpleTypedActorTemplate[Msg, Result](name) {
  private val aux = new UniqueSimpleTypedActorAsyncImpl[Msg, Result](name, f.andThen(Future(_)))
  override def !(m: => Msg): Future[Result] = aux ! m

  protected def describeMessage(m: Msg): String = aux.describeMessage(m)
}
