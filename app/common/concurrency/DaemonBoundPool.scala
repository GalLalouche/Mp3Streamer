package common.concurrency

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import scala.concurrent.duration.Duration

private object DaemonBoundPool {
  def apply(name: String, n: Int, keepAliveTime: Duration = Duration.Zero) = new ThreadPoolExecutor(
    0, // corePoolSize, i.e., minimum number of threads to keep alive.
    n.ensuring(_ > 0), // maximumPoolSize
    keepAliveTime.toNanos,
    TimeUnit.NANOSECONDS,
    new LinkedBlockingQueue[Runnable](),
    DaemonThreadFactory(s"<$name>'s ${if (n == 1) "single" else s"$n"}-threaded job queue"),
  )
}
