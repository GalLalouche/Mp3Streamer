package common.concurrency

import scala.concurrent.ExecutionContext

object ThreadlessContext extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = runnable.run()
  override def reportFailure(cause: Throwable): Unit = throw cause
}
