package common.concurrency

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

object DaemonFixedPool {
  def single(name: String): ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  def apply(name: String, n: Int): ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(n, DaemonThreadFactory(name)))
}
