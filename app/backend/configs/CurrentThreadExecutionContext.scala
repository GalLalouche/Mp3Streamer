package backend.configs

import scala.concurrent.ExecutionContext

object CurrentThreadExecutionContext extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = runnable.run()
  override def reportFailure(cause: Throwable): Unit = throw cause
}
