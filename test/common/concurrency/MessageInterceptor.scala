package common.concurrency
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import com.google.common.base.Stopwatch

import scala.annotation.tailrec
import scala.concurrent.duration.Duration


class MessageInterceptor[T] {
  private val q = new LinkedBlockingQueue[T]
  def intercept(t: T) {q offer t}
  @tailrec
  final def expectMessage(msg: T, timeout: Duration = Duration(100, TimeUnit.MILLISECONDS)) {
    if (q.contains(msg))
      return
    val watch = Stopwatch.createStarted()
    q.poll(timeout.toMillis, TimeUnit.MILLISECONDS) match {
      case null => throw new AssertionError(s"Expected message $msg but didn't get it. got $q instead")
      case t if t == msg => return
      case t =>
        q.offer(t)
        expectMessage(msg, Duration.apply(timeout.toMillis - watch.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS))
    }
  }

  def expectNone(timeout: Duration = Duration(100, TimeUnit.MILLISECONDS)) {
    Thread.sleep(100)
    val $ = q.poll(timeout.toMillis, TimeUnit.MILLISECONDS)
    if ($ != null)
      throw new AssertionError("Expected nothing but got: " + $)
  }
}
