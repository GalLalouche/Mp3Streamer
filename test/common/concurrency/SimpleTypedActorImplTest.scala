package common.concurrency

import backend.logging.ScribeUtils

import java.util.concurrent.Semaphore
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import common.concurrency.SimpleTypedActorImplTest.asyncAcquire
import common.test.AsyncAuxSpecs

class SimpleTypedActorImplTest extends AsyncFreeSpec with OneInstancePerTest with AsyncAuxSpecs {
  ScribeUtils.noLogs()
  "apply (sync)" - {
    "basic test" in {
      (SimpleTypedActor[String, Int]("MyName", _.length) ! "Foobar") shouldEventuallyReturn 6
    }
    "process requests in FIFO" in {
      val sb = new StringBuilder
      val semaphore = new Semaphore(0)
      val map = Map("1" -> new Semaphore(0), "2" -> new Semaphore(0))
      def appendToSb(i: Int) {
        sb.append(i.toString)
        semaphore.release()
      }
      val $ = SimpleTypedActor[String, Int](
        "MyName",
        m => {
          map(m).acquire()
          val $ = m.toInt
          appendToSb($)
          $
        },
      )

      // 1 is requested before 2
      $ ! "1"
      $ ! "2"

      // 2 is released before 1
      map("2").release()
      map("1").release()

      semaphore.acquire(2)
      // but order is 1 and then 2
      sb.toString shouldReturn "12"
    }

    "reports failures" in {
      (SimpleTypedActor[String, Int]("MyName", _.length) ! null)
        .checkFailure(_ shouldBe a[NullPointerException])
    }
  }

  "async" - {
    def futureLength(s: String): Future[Int] = Future(s.length)
    "basic test" in {
      (SimpleTypedActor.async[String, Int](
        "MyName",
        futureLength,
      ) ! "Foobar") shouldEventuallyReturn 6
    }
    "process requests in FIFO" in {
      val sb = new StringBuilder
      val semaphore = new Semaphore(0)
      val map = Map("1" -> new Semaphore(0), "2" -> new Semaphore(0))
      def appendToSb(i: Int) {
        sb.append(i.toString)
        semaphore.release()
      }
      val $ = SimpleTypedActor.async[String, Int](
        "MyName",
        asyncAcquire(map, appendToSb)(DaemonFixedPool("Async test pool", 1)),
      )

      // 1 is requested before 2
      $ ! "1"
      $ ! "2"

      // 2 is released before 1
      map("2").release()
      map("1").release()

      semaphore.acquire(2)
      // but order is 1 and then 2
      sb.toString shouldReturn "12"
    }

    "reports failures" - {
      "async failures" in {
        (SimpleTypedActor.async[String, Int]("MyName", futureLength) ! null)
          .checkFailure(_ shouldBe a[NullPointerException])
      }
      "sync failures" in {
        (SimpleTypedActor.async[String, Int](
          "MyName",
          _ => throw new AssertionError("foobar"),
        ) ! null).shouldFail()
      }
    }
  }
}

private object SimpleTypedActorImplTest {
  def asyncAcquire(map: Map[String, Semaphore], action: Int => Unit)(implicit
      ec: ExecutionContext,
  ): String => Future[Int] = { m =>
    for {
      _ <- Future(map(m).acquire())
      result <- Future(m.toInt)
      _ = action(result)
    } yield result
  }
}
