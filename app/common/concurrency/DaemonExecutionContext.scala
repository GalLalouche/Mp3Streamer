package common.concurrency

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object DaemonExecutionContext {
  def single(name: String): ExecutionContext = new AggressiveSingleThreadContext(name)
  def apply(name: String, n: Int, keepAlive: Duration = Duration.Zero): ExecutionContext =
    ExecutionContext.fromExecutorService(DaemonBoundPool(name, n, keepAlive))
}
