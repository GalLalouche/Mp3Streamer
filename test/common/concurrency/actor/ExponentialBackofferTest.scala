package common.concurrency.actor

import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AsyncFreeSpec

import scala.concurrent.duration.DurationInt

import common.test.AsyncAuxSpecs

class ExponentialBackofferTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private var x = 0
  private val $ = Actor.exponentialBackoff[Int, String](
    "test",
    maxRetries = 3,
    rateLimit = 1.milli,
  ) { i =>
    if (x < i) {
      x += 1
      throw new Exception()
    }
    i.toString
  }
  "Immediate success" in {
    $.!(0).map { res =>
      res shouldReturn "0"
      x shouldReturn 0
    }
  }
  "After 1 retry" in {
    $.!(1).map { res =>
      res shouldReturn "1"
      x shouldReturn 1
    }
  }
  "After 3 retries" in {
    $.!(3).map { res =>
      res shouldReturn "3"
      x shouldReturn 3
    }
  }
  "After retries, it fails" in {
    $.!(4).checkFailure { e =>
      x shouldReturn 4
      e.getCause should not be null
    }
  }
  "Sleeps correctly" in {
    val now = System.currentTimeMillis()
    val $ = Actor.exponentialBackoff[Int, String](
      "test",
      maxRetries = 3,
      rateLimit = 5.milli,
    ) { i =>
      if (x < i) {
        x += 1
        throw new Exception()
      }
      i.toString
    }
    $.!(100).checkFailure { _ =>
      System.currentTimeMillis() - now should be > 50L
    }
  }
}
