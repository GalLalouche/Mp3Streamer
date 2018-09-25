package common.concurrency

import java.util

import common.rich.RichT._
import common.rich.collections.RichMap._
import common.rich.func.ToMoreFunctorOps

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances

private class UniqueSimpleTypedActorImpl[Msg, Result](
    name: String, f: Msg => Result) extends SimpleTypedActor[Msg, Result]
    with ToMoreFunctorOps with FutureInstances {
  private val messages: util.Map[Msg, Future[Result]] = new util.HashMap()
  private implicit val service: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  private def clear(m: Msg): Unit = synchronized {messages.remove(m).ensuring(_ != null)}
  def !(m: => Msg): Future[Result] = synchronized {
    messages.getOrPutIfAbsent(m, Future(f(m)).listen(clear(m).const))
  }
}
