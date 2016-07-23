package common.concurrency

import java.util.concurrent.{Executors, ThreadFactory}

import common.rich.RichT._

import scala.concurrent.{ExecutionContext, Future}

/** It's a single threaded future factory basically */
trait SimpleTypedActor[Msg, Result] {
  protected def apply(m: Msg): Result
  protected val queue = Executors.newFixedThreadPool(1, new ThreadFactory() {
    override def newThread(r: Runnable) = {
      val $ = new Thread(r, s"${SimpleTypedActor.this.simpleName}'s actor thread")
      $.setDaemon(true)
      $
    }
  })
  def !(m: Msg): Future[Result] = Future(this.apply(m))(ExecutionContext.fromExecutorService(queue))
}
