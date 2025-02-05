package common.concurrency

import java.util.concurrent.Callable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

private class SingleThreadedJobQueue(queueName: String) {
  private val queue = DaemonBoundPool(queueName, n = 1, keepAliveTime = 1.minute)

  private lazy val asExecutionContext: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = queue.submit(runnable)
    override def reportFailure(cause: Throwable): Unit = {
      scribe.error(s"Error @ $queueName", cause)
      cause.printStackTrace()
    }
  }
  def apply(a: => Unit): Unit = queue.submit(new Callable[Unit] { override def call(): Unit = a })
}

private object SingleThreadedJobQueue {
  def executionContext(serviceName: String): ExecutionContext =
    new SingleThreadedJobQueue(serviceName).asExecutionContext
}
