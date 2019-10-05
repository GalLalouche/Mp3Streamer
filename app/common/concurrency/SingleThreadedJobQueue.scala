package common.concurrency

import java.util.concurrent.{Callable, Executors}

import scala.concurrent.ExecutionContext

import common.rich.RichT._

class SingleThreadedJobQueue(queueName: String) {
  private val queue = Executors.newFixedThreadPool(
    1, (r: Runnable) => new Thread(r, s"<$queueName>'s single threaded job queue").<|(_ setDaemon true))

  lazy val asExecutionContext: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = queue submit runnable
    override def reportFailure(cause: Throwable): Unit = ???
  }
  def apply(a: => Unit): Unit = queue submit new Callable[Unit] {override def call() = a}
}

object SingleThreadedJobQueue {
  def executionContext(serviceName: String): ExecutionContext =
    new SingleThreadedJobQueue(serviceName).asExecutionContext
}
