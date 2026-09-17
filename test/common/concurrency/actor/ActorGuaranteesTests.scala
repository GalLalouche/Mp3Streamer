package common.concurrency.actor

import java.util.concurrent.{CountDownLatch, Semaphore}

import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.kats.ToMoreMonadErrorOps.toMoreMonadErrorOps

import common.concurrency.DaemonExecutionContext
import common.rich.RichFuture.richFutureBlocking
import common.test.AsyncAuxSpecs

class ActorGuaranteesTests extends AsyncFreeSpec with OneInstancePerTest with AsyncAuxSpecs {
  implicit override val executionContext: ExecutionContext =
    DaemonExecutionContext("SimpleTypedActorImplTest", 8)

  trait TestBuilder {
    def name: String
    def actorBuilder: ActorBuilder[String, Int]
    def validateFailure: Boolean = true
  }
  def test(builder: TestBuilder): Unit = {
    def exception(str: String) = new IllegalStateException(s"Failed on <$str>")

    def success(actor: Actor[String, Int]): Unit =
      s"process requests in FIFO and not concurrently" in 10.assertParTimes {
        val map = Map("1" -> new Semaphore(0), "2" -> new Semaphore(0))
        val cdl = new CountDownLatch(2)
        val list = new ListBuffer[String]()

        // Ensures listen runs as part of the original message handling.
        implicit val ec: ExecutionContext.parasitic.type = ExecutionContext.parasitic
        // 1 is requested before 2
        actor.! { map("1").acquire(); list += "1"; "1" }.listenAny(cdl.countDown())
        actor.! { map("2").acquire(); list += "2"; "2" }.listenAny(cdl.countDown())

        // 2 is released before 1
        map("2").release()
        Thread.`yield`()
        map("1").release()
        Thread.`yield`()

        cdl.await()
        // but order is 1 and then 2
        list.toVector shouldReturn Vector("1", "2")
      }

    def failure(actor: Actor[String, Int]): Unit =
      s"Failures are handled by the Future" in 10.assertParTimes {
        val e = actor.!("42").getFailure
        if (builder.validateFailure) {
          e shouldBe an[IllegalStateException]
          e.getMessage shouldReturn "Failed on <42>"
        } else
          succeed
      }

    builder.name - {
      "sync" - {
        success(builder.actorBuilder(_.toInt))
        failure(builder.actorBuilder(s => throw exception(s)))
      }
      "async (immediate)" - {
        success(builder.actorBuilder.async(s => Future.successful(s.toInt)))
        failure(builder.actorBuilder.async(s => Future.failed(exception(s))))
      }
      "async (delayed)" - {
        success(builder.actorBuilder.async(s => Future(s.toInt)))
        failure(builder.actorBuilder.async(s => Future(throw exception(s))))
      }
      "producer" - {
        success(builder.actorBuilder.producer(42))
        failure(builder.actorBuilder.producer(throw exception("42")))
      }
    }
  }

  test(
    new TestBuilder {
      override def name: String = "simple"
      override def actorBuilder: ActorBuilder[String, Int] = Actor(name)
    },
  )
  test(
    new TestBuilder {
      override def name: String = "rate limited"
      override def actorBuilder: ActorBuilder[String, Int] = Actor.rateLimited(name, 1.millis)
    },
  )
  test(
    new TestBuilder {
      override def name: String = "unique"
      override def actorBuilder: ActorBuilder[String, Int] = Actor.unique(name)
    },
  )
  test(
    new TestBuilder {
      override def name: String = "exponential out of rate limited"
      override def actorBuilder: ActorBuilder[String, Int] = Actor.exponentialBackoff(
        name,
        maxRetries = 3,
        rateLimit = 1.millisecond,
      )
      // Failure it made up of the causes of all failures up to the point
      override def validateFailure = false
    },
  )
}
