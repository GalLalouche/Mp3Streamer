package common.concurrency.iterant

import scala.concurrent.ExecutionContext

private object ThreadlessContext extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = runnable.run()
  override def reportFailure(cause: Throwable): Unit = throw cause
}
