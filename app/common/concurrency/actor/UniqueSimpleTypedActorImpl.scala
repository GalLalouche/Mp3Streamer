package common.concurrency.actor

import scala.concurrent.{ExecutionContext, Future}

import common.concurrency.DaemonExecutionContext

private class UniqueSimpleTypedActorImpl[Msg, Result](
    name: String,
    f: Msg => Result,
) extends SimpleTypedActor[Msg, Result] {
  protected implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)
  private val aux = new UniqueSimpleTypedActorAsyncImpl[Msg, Result](name, m => Future(f(m)))
  override def !(m: => Msg): Future[Result] = aux ! m

  def void: SimpleActor[Msg] = aux.void
  protected def describeMessage(m: Msg): String = aux.describeMessage(m)
}
