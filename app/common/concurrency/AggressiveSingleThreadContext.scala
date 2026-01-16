package common.concurrency

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor}
import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.ExecutionContext

import common.rich.RichT.richT

private class AggressiveSingleThreadContext(queueName: String) extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = queue.submit(runnable)
  override def reportFailure(cause: Throwable): Unit = {
    scribe.error(s"Error @ $queueName", cause)
    cause.printStackTrace()
  }
  // We don't need the ElasticExecutor here since the max pool size is 1.
  private val queue = new ThreadPoolExecutor(
    0, // corePoolSize
    1, // maxPoolSize
    1, // keepAliveTime
    java.util.concurrent.TimeUnit.SECONDS,
    new LinkedBlockingQueue[Runnable](),
    new Thread(_, threadName()).<|(_.setDaemon(true)),
  )

  private def threadName() =
    s"AggressiveSingleThreadContext-$queueName (Generation #${counter.getAndIncrement()})"
  private val counter = new AtomicLong(1)
}
