package common.concurrency

import java.util.concurrent.Executors

import common.rich.RichT._

import scala.concurrent.ExecutionContext

class SingleThreadedJobQueue(queueName: String) {
  lazy val asExecutionContext: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = queue submit runnable
    override def reportFailure(cause: Throwable): Unit = ???
  }

  private val queue = Executors.newFixedThreadPool(
    1, (r: Runnable) => {
      val $ = new Thread(r, s"$queueName's single threaded job queue")
      $ setDaemon true
      $
    })
  def apply(a: => Unit): Unit = queue submit a
}
