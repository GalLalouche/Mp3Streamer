package common.concurrency

import java.util

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFunctorOps._

import common.rich.RichT._

private class UniqueSimpleTypedActorImpl[Msg, Result](
    name: String,
    f: Msg => Result,
) extends SimpleTypedActor[Msg, Result] {
  private val messages: util.Map[Msg, Future[Result]] = new util.HashMap()
  private implicit val service: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  private def clear(m: Msg): Unit = synchronized(messages.remove(m).ensuring(_ != null))
  override def !(m: => Msg): Future[Result] = synchronized {
    lazy val msg = m
    messages.compute(
      msg,
      (_, result) =>
        if (result == null)
          // TODO clear in cases of failures too
          Future(f(msg)).listen(clear(msg).const)
        else {
          scribe.trace(s"Ignoring non-unique msg <$msg>")
          result
        },
    )
  }
}
