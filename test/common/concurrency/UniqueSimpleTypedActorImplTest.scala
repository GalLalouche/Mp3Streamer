package common.concurrency

import java.util.concurrent.Semaphore

import common.AuxSpecs
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

class UniqueSimpleTypedActorImplTest extends FreeSpec with OneInstancePerTest with AuxSpecs {
  "unique" in {
    val sb = new StringBuilder
    val semaphore = new Semaphore(0)
    val $ = SimpleTypedActor.unique[String, Unit]("MyName", m => {
      semaphore.acquire()
      sb.append(m)
    })
    val f = $ ! "foo"
    val g = $ ! "foo"
    f should be theSameInstanceAs g
    semaphore.release(1)
    Await.result(f, 1 second)
    sb.toString shouldReturn "foo"

    // Verifies clear
    val h = $ ! "foo"
    semaphore.release(1)
    Await.result(h, 1 second)
    sb.toString shouldReturn "foofoo"
  }
}
