package common.concurrency.actor

import scala.concurrent.{ExecutionContext, Future}

private class UniqueSimpleTypedActorImpl[Msg, Result](
    name: String,
    f: Msg => Result,
) extends SimpleTypedActor[Msg, Result] {
  private val aux = new UniqueSimpleTypedActorAsyncImpl[Msg, Result](name, m => Future(f(m)))
  protected implicit val ec: ExecutionContext = aux.ec
  override def !(m: => Msg): Future[Result] = aux ! m

  def void: SimpleActor[Msg] = aux.void
  protected def describeMessage(m: Msg): String = aux.describeMessage(m)
}
