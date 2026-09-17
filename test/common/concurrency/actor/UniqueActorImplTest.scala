package common.concurrency.actor

import java.util.concurrent.Semaphore

import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

import common.concurrency.DaemonExecutionContext
import common.rich.RichFuture.richFutureBlocking
import common.test.AuxSpecs

class UniqueActorImplTest extends AnyFreeSpec with OneInstancePerTest with AuxSpecs {
  implicit val executionContext: ExecutionContext = DaemonExecutionContext("ElasticExecutorTest", 8)
  "unique" in 1000.parTimes {
    val sb = new StringBuilder
    val semaphore = new Semaphore(0)
    var counter = 0
    val $ = Actor.unique[String, Int]("MyName") { m =>
      semaphore.acquire()
      counter += 1
      sb.append(m)
      sb.append(counter)
      m.length + counter
    }
    val f = $ ! "foo"
    val g = $ ! "foo"
    semaphore.release(1)
    f.get(1 second) shouldReturn 4
    g.isCompleted shouldReturn true
    g.get shouldReturn 4
    sb.toString shouldReturn "foo1"

    // Verifies clear
    val h = $ ! "foo"
    semaphore.release(1)
    h.get(1 second) shouldReturn 5
    sb.toString shouldReturn "foo1foo2"
  }

  "failures" in 100.parTimes {
    val semaphore = new Semaphore(0)
    var counter = 0
    val $ = Actor.unique[String, Unit]("MyName") { m =>
      semaphore.acquire()
      counter += 1
      throw new Exception("Whoopsies" + m)
    }
    val f = $ ! "foo"
    semaphore.release()
    val e = the[Exception] thrownBy (Await.result(f, 1 second))
    e.getMessage shouldReturn "Whoopsiesfoo"
    val g = $ ! "foo"
    semaphore.release()
    val e2 = the[Exception] thrownBy (Await.result(g, 1 second))
    e2.getMessage shouldReturn "Whoopsiesfoo"
    counter shouldReturn 2
  }
}
