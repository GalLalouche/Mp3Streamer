package common.concurrency

import java.util.concurrent.Executors
import common.rich.primitives.RichBoolean._

import common.rich.RichT._
import common.rich.func.ToMoreFunctorOps
import scalaz.std.FutureInstances

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/** It's a single threaded future factory basically */
trait SimpleTypedActor[Msg, Result]
    extends ToMoreFunctorOps with FutureInstances {
  protected def apply(m: Msg): Result
  protected def unique = false
  // TODO reuse SingleThreadedJobQueue?
  private val messages: mutable.Map[Msg, Future[Result]] = new mutable.HashMap()
  private implicit val service: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1, (r: Runnable) => {
    val $ = new Thread(r, s"${SimpleTypedActor.this.simpleName}'s actor thread")
    $.setDaemon(true)
    $
  }))
  private def clear(m: Msg): Unit = synchronized {messages.remove(m).ensuring(_.isDefined)}
  def !(m: => Msg): Future[Result] = synchronized {
    lazy val $ = Future(this (m))
    if (unique) {
      if (messages.contains(m).isFalse)
        messages.put(m, $.listen(clear(m).const))
      messages(m)
    } else
      $
  }
}
