package common.concurrency

import java.util.concurrent.Semaphore

import common.AuxSpecs
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

class SimpleTypedActorImplTest extends FreeSpec with OneInstancePerTest with AuxSpecs {
  "basic test" in {
    val $ = SimpleTypedActor[String, Int]("MyName", _.length) ! "Foobar"
    Await.result($, 1 second) shouldReturn 6
  }
  "process requests in FIFO" in {
    val sb = new StringBuilder
    val semaphore = new Semaphore(0)
    val map = Map("1" -> new Semaphore(0), "2" -> new Semaphore(0))
    def appendToSb(i: Int) {
      sb append i.toString
      semaphore.release()
    }
    val $ = SimpleTypedActor[String, Int]("MyName", m => {
      map(m).acquire()
      val $ = m.toInt
      appendToSb($)
      $
    })

    // 1 is requested before 2
    $ ! "1"
    $ ! "2"

    // 2 is released before 1
    map("2").release()
    map("1").release()

    semaphore acquire 2
    // but order is 1 and then 2
    sb.toString shouldReturn "12"
  }

  "unique" in {
    val sb = new StringBuilder
    val semaphore = new Semaphore(0)
    val $ = SimpleTypedActor[String, Unit]("MyName", m => {
      semaphore.acquire()
      sb.append(m)
    }, unique = true)
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
