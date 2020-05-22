package common.concurrency

import java.util.concurrent.Semaphore

import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}

import scala.language.postfixOps

import common.test.AsyncAuxSpecs

class SimpleTypedActorImplTest extends AsyncFreeSpec with OneInstancePerTest with AsyncAuxSpecs {
  "basic test" in {
    (SimpleTypedActor[String, Int]("MyName", _.length) ! "Foobar").map(_ shouldReturn 6)
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
}
