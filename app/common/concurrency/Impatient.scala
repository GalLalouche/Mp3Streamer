package common.concurrency

import java.util.concurrent.{Executors, TimeoutException}

import scala.concurrent.duration.Duration

class Impatient[Result](d: Duration) {
  private val service = Executors.newFixedThreadPool(1)
  def apply(task: => Result): Option[Result] = {
    val future = service.submit(task)
    try Some(future.get(d.length, d.unit))
    catch {
      case e: TimeoutException =>
        assert(future.cancel(true))
        None
    }
  }
}
