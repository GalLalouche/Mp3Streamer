package common.concurrency

import java.util.concurrent.Executors

import common.rich.RichT._

import scala.concurrent.ExecutionContext

class SingleThreadedJobQueue {
  lazy val asExecutionContext: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = queue submit runnable
    override def reportFailure(cause: Throwable): Unit = ???
  }

  private val queue = Executors.newFixedThreadPool(
    1, (r: Runnable) => {
      val $ = new Thread(r, s"${this.simpleName}'s actor thread")
      $ setDaemon true
      $
    })
  def apply(a: => Unit): Unit = queue submit a
}
