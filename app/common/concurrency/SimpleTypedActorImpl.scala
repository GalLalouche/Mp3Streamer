package common.concurrency

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFunctorOps

private class SimpleTypedActorImpl[Msg, +Result](
    name: String,
    f: (Msg, SimpleTypedActor[Msg, Result]) => Result,
) extends SimpleTypedActor[Msg, Result] {
  def this(name: String, f: Msg => Result) =
    this(name, (m: Msg, _: SimpleTypedActor[Msg, Result]) => f(m))
  protected implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)
  override def !(m: => Msg): Future[Result] = Future(f(m, this))

  def void: SimpleActor[Msg] = SimpleTypedActorImpl.this.!(_).void
}
