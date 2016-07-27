package common.concurrency

import scala.concurrent.ExecutionContext

object SingleThreadedExecutionContext {
  implicit val ec: ExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable): Unit = ???
    override def execute(runnable: Runnable): Unit = runnable.run()
  }
}
