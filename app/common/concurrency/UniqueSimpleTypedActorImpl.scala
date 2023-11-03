package common.concurrency

import java.util

import backend.logging.Logger

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFunctorOps._

import common.rich.collections.RichMap._
import common.rich.RichT._

private class UniqueSimpleTypedActorImpl[Msg, Result](
    name: String,
    f: Msg => Result,
    logger: Logger,
) extends SimpleTypedActor[Msg, Result] {
  private val messages: util.Map[Msg, Future[Result]] = new util.HashMap()
  private implicit val service: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  private def clear(m: Msg): Unit = synchronized(messages.remove(m).ensuring(_ != null))
  def !(m: => Msg): Future[Result] = synchronized {
    lazy val msg = m
    if (messages.containsKey(msg))
      logger.verbose(s"Ignoring non-unique msg <$msg>")
    // TODO clear in cases of failures too
    messages.getOrPutIfAbsent(msg, Future(f(msg)).listen(clear(msg).const))
  }
}
