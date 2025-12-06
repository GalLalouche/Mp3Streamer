package common.concurrency

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

import org.jboss.threads.EnhancedQueueExecutor

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.duration.Duration.Zero
import scala.jdk.DurationConverters.ScalaDurationOps

import common.rich.func.kats.ToMoreFoldableOps.toMoreFoldableOps

import common.rich.RichT.richT

/**
 * A combination of CachedThreadPool and FixedThreadPool. New threads will be created up to the
 * bound only if there are no idle ones, and these will be individually killed off one-by-one if no
 * tasks are available for the keepAlive duration. Doesn't use round-robin or FIFO, so threads may
 * actually be killed if there are fewer tasks than there are threads. If there are no free threads
 * and the bound limit has been reached, it will use an unbounded blocking queue.
 */
object ElasticExecutor {
  def apply(
      name: String,
      keepAlive: Duration,
      bound: Int,
      daemon: Boolean,
  ): ExecutorServiceWithStats = new EnhancedQueueExecutor.Builder()
    .setCorePoolSize(0)
    .setMaximumPoolSize(bound.requiring(_ >= 0))
    .setKeepAliveTime(
      // Yes, it's pretty stupid for a duration of zero to be treated as potentially infinite, but
      // jboss-threads is pretty stupid and doesn't actually allow for zero duration.
      keepAlive.safeCast[FiniteDuration].filter(_ > Zero).mapHeadOrElse(_.toJava, DefaultKeepAlive),
    )
    .setThreadFactory(
      if (bound == 1) new SingleThreadFactory(name, daemon)
      else new MultipleThreadFactory(bound, name, daemon),
    )
    .setGrowthResistance(0) // Disables the "Standard Java" behavior of queueing before growing.
    .build
    .|>(ExecutorServiceWithStats.from(_)(_.getPoolSize, _.getLargestPoolSize))

  // It might start killing out threads in 99 years, but this is a future me problem.
  private val DefaultKeepAlive = java.time.Duration.ofDays(365 * 99)

  private abstract class ThreadFactoryTemplate(daemon: Boolean) extends ThreadFactory {
    private val n = new AtomicInteger(1)
    protected def threadName(n: Int): String
    override def newThread(r: Runnable): Thread =
      new Thread(r, threadName(n.getAndIncrement())).<|(_.setDaemon(daemon))
  }

  private class SingleThreadFactory(name: String, daemon: Boolean)
      extends ThreadFactoryTemplate(daemon) {
    protected override def threadName(n: Int) = s"<$name> single-threaded worker (Generation #$n)"
  }

  private class MultipleThreadFactory(bound: Int, name: String, daemon: Boolean)
      extends ThreadFactoryTemplate(daemon) {
    protected override def threadName(n: Int) = s"<$name> $bound-bounded pool worker #$n"
  }
}
