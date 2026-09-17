package common.concurrency.actor

import java.util

import scala.concurrent.Future

import common.rich.func.kats.PureError
import common.rich.func.kats.ToMoreMonadErrorOps.toMoreMonadErrorOps

private final class UniqueActorImpl[Msg, Result](
    name: String,
    f: ActorFunction[Msg, Result],
) extends ActorTemplate[Msg, Result](name) {
  private val messages: util.Map[Msg, Future[Result]] = new util.HashMap()
  private def clear(m: Msg): Unit = synchronized(messages.remove(m).ensuring(_ != null))
  override def !(m: => Msg): Future[Result] = Future.delegate {
    synchronized {
      lazy val msg = m
      messages.compute(
        msg,
        (_, result) =>
          if (result == null)
            PureError.tryWrap(f(this, msg)).listenAny(clear(msg))
          else {
            scribe.trace(s"$name: Ignoring non-unique msg <${describeMessage(msg)}>")
            result
          },
      )
    }
  }

  def describeMessage(m: Msg): String = s"msg <$m>"
}
