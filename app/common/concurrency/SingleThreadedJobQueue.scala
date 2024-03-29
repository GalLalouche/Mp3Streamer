package common.concurrency

import java.util.concurrent.{Callable, Executors}

import scala.concurrent.ExecutionContext

private class SingleThreadedJobQueue(queueName: String) {
  private val queue = Executors.newFixedThreadPool(
    1,
    DaemonThreadFactory(s"<$queueName>'s single threaded job queue"),
  )

  private lazy val asExecutionContext: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = queue.submit(runnable)
    override def reportFailure(cause: Throwable): Unit = {
      println(s"Error @ $queueName")
      cause.printStackTrace()
    }
  }
  def apply(a: => Unit): Unit = queue.submit(new Callable[Unit] { override def call(): Unit = a })
}

private object SingleThreadedJobQueue {
  def executionContext(serviceName: String): ExecutionContext =
    new SingleThreadedJobQueue(serviceName).asExecutionContext
}
