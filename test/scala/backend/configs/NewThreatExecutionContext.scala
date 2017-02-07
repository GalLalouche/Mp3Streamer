package backend.configs

import scala.concurrent.ExecutionContext

// Every request in a new thread
object NewThreatExecutionContext extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = new Thread(runnable).start()
  override def reportFailure(cause: Throwable): Unit = {
    throw cause
  }
}
