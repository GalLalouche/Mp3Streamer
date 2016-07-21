package common

import org.scalatest.FreeSpec
import RichFuture._
import common.concurrency.SingleThreadedExecutionContext

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class RichFutureTest extends FreeSpec with AuxSpecs {
  val success: Future[Int] = Future successful 1
  val failure: Future[Int] = Future failed new Exception("Derp")
  val fakeContext = SingleThreadedExecutionContext.ec
  def shouldFail(f: Future[Any]) = {
    Await.ready(f, Duration.Inf)
    val t = f.value.get
    t.isFailure shouldReturn true
  }
  def get[T](f: Future[T]): T = Await.result(f, Duration.Inf)
  "RichFuture" - {
    "get" - {
      "success" in {
        val f = Future {
          Thread.sleep(10)
          5
        }
        f.get shouldReturn 5
      }
      "failure" in {
        val f = Future {
          Thread.sleep(10)
          throw new Exception()
        }
        an[Exception] should be thrownBy f.get

      }
    }
    "filterWith" - {
      "success" in {
        success.filterWith(_ > 0, "Doesn't matter").get shouldReturn 1
      }
      "failure" in {
        val f = success.filterWith(_ < 0, "Failure message")
        shouldFail(f)
        f.value.get.failed.get.getMessage shouldReturn "Failure message"
      }
    }
    "orElse" - {
      "success" in {
        success.orElse(???).get shouldReturn 1
      }
      "failure" in {
        failure.orElse(10).get shouldReturn 10
      }
    }
    "orElseTry" - {
      "success" in {
        success.orElseTry(???).get shouldReturn 1
      }
      "failure" - {
        "to success" in {
          failure.orElseTry(Future successful 10).get shouldReturn 10
        }
        "to failure" in {
          shouldFail(failure.orElseTry(failure))
        }
      }
    }
    "onEnd" - {
      "success" in {
        success.onEnd(Future successful 10).get shouldReturn 10
      }
      "failure" - {
        val f: Future[Int] = failure.onEnd(Future successful 10)
        an[Exception] should be thrownBy f.get
      }
      "in order" in {
        val list = new ListBuffer[Int]
        def add(i: Int) = {
          Future(list.+=(i))
        }
        add(0).onEnd(add(1)).onEnd(add(2)).get
        list.toList shouldReturn List(0, 1, 2)
      }
    }
  }
  "RichOptionFuture" - {
    val success: Future[Option[Int]] = Future successful Some(1)
    val failure: Future[Option[Int]] = Future failed new Exception("Derp")
    val none: Future[Option[Int]] = Future successful None
    "ifNone" - {
      "success" - {
        "some" in {
          success.ifNone(???).get shouldReturn 1
        }
        "none" in {
          none.ifNone(10).get shouldReturn 10
        }
      }
      "failure" in {
        shouldFail(failure.ifNone(10))
      }
    }
    "ifNoneTry" - {
      "success" - {
        "some" in {
          success.ifNoneTry(???).get shouldReturn 1
        }
        "none" in {
          none.ifNoneTry(Future successful 10).get shouldReturn 10
        }
      }
      "failure" in {
        shouldFail(failure.ifNoneTry(Future successful 10))
      }

    }
  }
}
