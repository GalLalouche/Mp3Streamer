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
    val watch = new Stopwatch().start()
    q.poll(timeout.toMillis, TimeUnit.MILLISECONDS) match {
      case null => throw new AssertionError(s"Expected message $msg but didn't get it. got $q instead")
      case t if t == msg => return
      case t =>
        q.offer(t)
        expectMessage(msg, Duration.apply(timeout.toMillis - watch.elapsedMillis(), TimeUnit.MILLISECONDS))
    }
  }
}
