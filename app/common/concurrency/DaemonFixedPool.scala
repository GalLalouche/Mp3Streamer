package common.concurrency

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

import common.rich.RichT._

object DaemonFixedPool {
  def single(name: String): ExecutionContext = SingleThreadedJobQueue.executionContext(name)
  def apply(name: String, n: Int): ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(
      n, new Thread(_: Runnable, name).<|(_.setDaemon(true))))
}
