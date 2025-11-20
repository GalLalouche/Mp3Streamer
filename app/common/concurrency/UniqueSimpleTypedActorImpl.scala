package common.concurrency

import java.util

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFunctorOps

import common.rich.RichT._

private class UniqueSimpleTypedActorImpl[Msg, Result](
    name: String,
    f: Msg => Result,
) extends SimpleTypedActor[Msg, Result] {
  private val messages: util.Map[Msg, Future[Result]] = new util.HashMap()
  protected implicit val ec: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  private def clear(m: Msg): Unit = synchronized(messages.remove(m).ensuring(_ != null))
  final override def !(m: => Msg): Future[Result] = synchronized {
    lazy val msg = m
    messages.compute(
      msg,
      (_, result) =>
        if (result == null)
          Future(f(msg)).<|(_.onComplete(clear(msg).const))
        else {
          scribe.trace(s"$name: Ignoring non-unique msg <${describeMessage(msg)}>")
          result
        },
    )
  }

  def void: SimpleActor[Msg] = UniqueSimpleTypedActorImpl.this.!(_).void

  protected def describeMessage(m: Msg): String = s"msg <$m>"
}
