package common.concurrency

import scala.concurrent.Future
import common.rich.RichT._
import scala.concurrent.ExecutionContext
import java.util.concurrent.ThreadFactory
import java.util.concurrent.Executors

trait FutureFactory[Msg, Result] {
  def apply(m: Msg): Result
  private val queue = Executors.newFixedThreadPool(1, new ThreadFactory() {
    override def newThread(r: Runnable) = {
      val $ = new Thread(r, s"${FutureFactory.this.simpleName}'s actor thread")
      $.setDaemon(true)
      $
    }
  })
  def !(m: Msg): Future[Result] = Future.apply(this.apply(m))(ExecutionContext.fromExecutorService(queue)) 
}