package common.concurrency
import java.util.concurrent.{Semaphore, LinkedBlockingDeque, TimeUnit}

import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}

class ProgressObservableTest extends FreeSpec with OneInstancePerTest with Matchers {
  val q = new LinkedBlockingDeque[String]()
  "invokes sink" in {
    val $ = new ProgressObservable {
      override protected def apply(sink: String => Unit) { sink("foobar") }
    }
    $ ! q.push
    q.poll(1000, TimeUnit.SECONDS) should be === "foobar"
  }
  "uses a different thread for sink" in {
    val s1 = new Semaphore(0)
    val s2 = new Semaphore(0)
    val $ = new ProgressObservable {
      override protected def apply(sink: String => Unit) {
        assert(s1.tryAcquire(1000, TimeUnit.SECONDS))
        s2.release()
      }
    }
    $ ! q.push
    s1.release()
    assert(s2.tryAcquire(1000, TimeUnit.SECONDS))
  }
}
