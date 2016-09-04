package common.concurrency

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import com.google.common.base.Stopwatch
import rx.lang.scala.Observer

import scala.annotation.tailrec
import scala.concurrent.duration.Duration


class MessageInterceptor[T] extends Observer[T] {
  def clear(): Unit = q.clear()

  private val q = new LinkedBlockingQueue[T]
  def intercept(t: T): Unit = q offer t
  override def onNext(value: T): Unit = intercept(value)
  def expectMessage(msg: T, timeout: Duration = Duration(100, TimeUnit.MILLISECONDS)) {
    @tailrec
    def aux(msg: T, timeout: Duration = Duration(100, TimeUnit.MILLISECONDS), parsedQ: List[T]): Unit = {
      if (q.contains(msg))
        return
      val watch = Stopwatch.createStarted()
      q.poll(timeout.toMillis, TimeUnit.MILLISECONDS) match {
        case null => throw new AssertionError(s"Expected message $msg but didn't get it. got ${parsedQ.reverse} instead")
        case t if t == msg => ()
        case t =>
          aux(msg, Duration.apply(timeout.toMillis - watch.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS), t :: parsedQ)
      }
    }
    aux(msg, timeout, Nil)
  }

  def expectNone(timeout: Duration = Duration(100, TimeUnit.MILLISECONDS)) {
    Thread.sleep(100)
    val $ = q.poll(timeout.toMillis, TimeUnit.MILLISECONDS)
    if ($ != null)
      throw new AssertionError("Expected nothing but got: " + $)
  }
}
