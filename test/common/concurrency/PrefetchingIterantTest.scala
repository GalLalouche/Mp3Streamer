package common.concurrency

import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.{RichOptionT, RichStreamT}
import common.rich.func.BetterFutureInstances._
import common.rich.func.RichStreamT.richStreamT
import scalaz.OptionT

import common.concurrency.Iterant.fromStream
import common.test.AsyncAuxSpecs

class PrefetchingIterantTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  implicit override def executionContext: ExecutionContext = ThreadlessContext

  private class TogellableProducer extends SimpleTypedActor[Unit, Option[Int]] {
    var counter = 1
    private var stopped = false
    def stop(): Unit = stopped = true
    override def !(m: => Unit) =
      if (stopped)
        OptionT.none[Future, Int].run
      else {
        val $ = counter
        counter += 1
        RichOptionT.pointSome.apply($).run
      }
  }

  private val actor = new TogellableProducer

  private val $ = Iterant.prefetching(fromStream(RichStreamT.fillM(OptionT(actor.!(Unit)))), 10)

  "starts on creation" in {
    actor.stop()
    $.toStream.take(10).toStream shouldEventuallyReturn 1.to(10).toStream
  }
  "Does not fetch more than is needed" in {
    actor.stop()
    $.toStream.take(10).toStream shouldEventuallyReturn 1.to(10).toStream
    actor.counter should be <= 12
  }
  "Should fetch more after getting" in {
    $.batchStep(10).map(_._1) shouldEventuallyReturn 1.to(10)
    actor.counter should be >= 20
  }
  "toStream should also prefetch more after getting" in {
    $.toStream.unconsBatch(10).map(_._1) shouldEventuallyReturn 1.to(10)
    actor.counter should be >= 20
  }
}
