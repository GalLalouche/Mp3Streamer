package common.concurrency

import java.util.concurrent.Semaphore

import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

import common.test.AuxSpecs

class UniqueSimpleTypedActorImplTest extends AnyFreeSpec with OneInstancePerTest with AuxSpecs {
  "unique" in {
    val sb = new StringBuilder
    val semaphore = new Semaphore(0)
    val $ = SimpleTypedActor.unique[String, Unit](
      "MyName",
      m => {
        semaphore.acquire()
        sb.append(m)
      },
    )
    val f = $ ! "foo"
    val g = $ ! "foo"
    (f should be).theSameInstanceAs(g)
    semaphore.release(1)
    Await.result(f, 1 second)
    sb.toString shouldReturn "foo"

    // Verifies clear
    val h = $ ! "foo"
    semaphore.release(1)
    Await.result(h, 1 second)
    sb.toString shouldReturn "foofoo"
  }

  "failures" in {
    val semaphore = new Semaphore(0)
    var counter = 0
    val $ = SimpleTypedActor.unique[String, Unit](
      "MyName",
      m => {
        semaphore.acquire()
        counter += 1
        throw new Exception("Whoopsies" + m)
      },
    )
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
