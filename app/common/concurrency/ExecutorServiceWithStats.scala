package common.concurrency

import java.util
import java.util.concurrent.{Callable, ExecutorService, Future, TimeUnit}

trait ExecutorServiceWithStats extends ExecutorService {
  def getPoolSize: Int
  def getLargestPoolSize: Int
}

private object ExecutorServiceWithStats {
  // IntelliJ's type inference doesn't work without this helper method for some reason.
  def from[E <: ExecutorService](underlying: E)(
      getPoolSizeImpl: E => Int,
      getLargestPoolSizeImpl: E => Int,
  ): ExecutorServiceWithStats = new Impl(underlying, getPoolSizeImpl, getLargestPoolSizeImpl)

  private class Impl[E <: ExecutorService](
      underlying: E,
      getPoolSizeImpl: E => Int,
      getLargestPoolSizeImpl: E => Int,
  ) extends ExecutorServiceWithStats {
    override def shutdown(): Unit = underlying.shutdown()
    override def shutdownNow(): util.List[Runnable] = underlying.shutdownNow()
    override def isShutdown: Boolean = underlying.isShutdown
    override def isTerminated: Boolean = underlying.isTerminated
    override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean =
      underlying.awaitTermination(timeout, unit)
    override def submit[T](task: Callable[T]): Future[T] = underlying.submit(task)
    override def submit[T](task: Runnable, result: T): Future[T] = underlying.submit(task, result)
    override def submit(task: Runnable): Future[_] = underlying.submit(task)
    override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]]): util.List[Future[T]] =
      underlying.invokeAll(tasks)
    override def invokeAll[T](
        tasks: util.Collection[_ <: Callable[T]],
        timeout: Long,
        unit: TimeUnit,
    ): util.List[Future[T]] = underlying.invokeAll(tasks, timeout, unit)
    override def invokeAny[T](tasks: util.Collection[_ <: Callable[T]]): T =
      underlying.invokeAny(tasks)
    override def invokeAny[T](
        tasks: util.Collection[_ <: Callable[T]],
        timeout: Long,
        unit: TimeUnit,
    ): T = underlying.invokeAny(tasks, timeout, unit)
    override def execute(command: Runnable): Unit = underlying.execute(command)
    override def getPoolSize: Int = getPoolSizeImpl(underlying)
    override def getLargestPoolSize: Int = getLargestPoolSizeImpl(underlying)
  }
}
