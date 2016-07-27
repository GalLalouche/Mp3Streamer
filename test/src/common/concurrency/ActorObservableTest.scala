package common.concurrency
import java.util.concurrent.{LinkedBlockingDeque, Semaphore, TimeUnit}

import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import rx.lang.scala.{Observable, Subscription}

class ActorObservableTest extends FreeSpec with OneInstancePerTest with Matchers {
  val q = new LinkedBlockingDeque[String]()
  "invokes sink" in {
    val $ = new ActorObservable[String]() {
      override protected def apply(): Observable[String] =
        Observable.create[String](o => {
          o onNext "foobar"
          Subscription.apply()
        })
    }
    $ ! new SimpleActor[String] {override protected def apply(m: String) {q push m }}
    q.poll(1000, TimeUnit.SECONDS) should be === "foobar"
  }
  "uses a different thread for sink" in {
    val s1 = new Semaphore(0)
    val s2 = new Semaphore(0)
    val $ = new ActorObservable[() => Unit] {
      override protected def apply() = Observable.create[() => Unit](o => {
        o.onNext(() => {
          assert(s1.tryAcquire(1, TimeUnit.SECONDS), "Could not acquire s1 in time")
          s2.release()
        })
        Subscription.apply()
      })
    } ! new SimpleActor[() => Unit] {
      override protected def apply(m: () => Unit) {m.apply() }
    }
    s1.release()
    assert(s2.tryAcquire(1, TimeUnit.SECONDS), "Could not acquire s2 in time")
  }
}
