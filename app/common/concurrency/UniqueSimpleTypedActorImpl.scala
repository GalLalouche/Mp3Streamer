package common.concurrency

import java.util

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import common.rich.func.ToMoreFunctorOps._

import common.rich.RichT._
import common.rich.collections.RichMap._

private class UniqueSimpleTypedActorImpl[Msg, Result](
    name: String, f: Msg => Result) extends SimpleTypedActor[Msg, Result] {
  private val messages: util.Map[Msg, Future[Result]] = new util.HashMap()
  private implicit val service: ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  private def clear(m: Msg): Unit = synchronized {messages.remove(m).ensuring(_ != null)}
  def !(m: => Msg): Future[Result] = synchronized {
    messages.getOrPutIfAbsent(m, Future(f(m)).listen(clear(m).const))
  }
}
