package common.concurrency.actor

import java.util

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFunctorOps
import common.rich.func.kats.ToMoreMonadErrorOps.toMoreMonadErrorOps

import common.concurrency.DaemonExecutionContext

private final class UniqueSimpleTypedActorAsyncImpl[Msg, Result](
    name: String,
    f: Msg => Future[Result],
) extends SimpleTypedActor[Msg, Result] {
  private val messages: util.Map[Msg, Future[Result]] = new util.HashMap()
  protected implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)
  private def clear(m: Msg): Unit = synchronized(messages.remove(m).ensuring(_ != null))
  override def !(m: => Msg): Future[Result] = synchronized {
    lazy val msg = m
    messages.compute(
      msg,
      (_, result) =>
        if (result == null)
          f(msg).listenAny(clear(msg))
        else {
          scribe.trace(s"$name: Ignoring non-unique msg <${describeMessage(msg)}>")
          result
        },
    )
  }

  def void: SimpleActor[Msg] = UniqueSimpleTypedActorAsyncImpl.this.!(_).void

  def describeMessage(m: Msg): String = s"msg <$m>"
}
