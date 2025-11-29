package common.concurrency

import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AsyncFreeSpec

import scala.concurrent.{ExecutionContext, Future}

import cats.data.OptionT

import common.test.AsyncAuxSpecs

class PrefetchingIterantTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  implicit override def executionContext: ExecutionContext = ThreadlessContext

  private class ToggleableProducer extends SimpleTypedActor[Unit, Option[Int]] {
    var counter = 1
    private var stopped = false
    def stop(): Unit = stopped = true
    override def !(m: => Unit): Future[Option[Int]] =
      if (stopped)
        OptionT.none[Future, Int].value
      else {
        val $ = counter
        counter += 1
        OptionT.some[Future]($).value
      }
  }

  private val actor = new ToggleableProducer

  private val $ : Iterant[Future, Int] =
    Iterant.prefetching(Iterant.unfold(OptionT(actor ! ())), 10)

  "starts on creation" in {
    actor.stop()
    $.take(10).toSeq shouldEventuallyReturn 1.to(10)
  }
  "Does not fetch more than is needed" in {
    actor.stop()
    $.take(10).toSeq shouldEventuallyReturn 1.to(10)
    actor.counter should be <= 12
  }
  "Should fetch more after getting" in {
    $.batchStep(10).map(_._1) shouldEventuallyReturn 1.to(10)
    actor.counter should be >= 20
  }
  "toSeq should also prefetch more after getting" in {
    $.take(10).toSeq shouldEventuallyReturn 1.to(10)
    actor.counter should be >= 20
  }
}
