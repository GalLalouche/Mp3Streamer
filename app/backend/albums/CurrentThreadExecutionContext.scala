package backend.albums

import scala.concurrent.ExecutionContext

private object CurrentThreadExecutionContext extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = runnable.run()
  override def reportFailure(cause: Throwable): Unit = throw cause
}
