package common.concurrency

import java.util.concurrent.{LinkedBlockingQueue, ThreadFactory, ThreadPoolExecutor, TimeUnit}

import scala.concurrent.duration.Duration

import common.rich.RichT.richT

private object DaemonBoundPool {
  def apply(name: String, n: Int, keepAliveTime: Duration = Duration.Zero) = new ThreadPoolExecutor(
    0, // corePoolSize, i.e., minimum number of threads to keep alive.
    n.requiring(_ > 0), // maximumPoolSize
    keepAliveTime.toNanos,
    TimeUnit.NANOSECONDS,
    new LinkedBlockingQueue[Runnable](),
    new ThreadFactory {
      val threadName = s"<$name>'s ${if (n == 1) "single" else s"$n"}-threaded job queue"
      override def newThread(r: Runnable) = new Thread(r, threadName).<|(_.setDaemon(true))
    },
  )
}
