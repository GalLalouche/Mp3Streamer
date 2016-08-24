package common.concurrency

import java.util.concurrent.Semaphore

import common.AuxSpecs
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FreeSpec, OneInstancePerTest}
import common.rich.RichT._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

class SimpleTypedActorTest extends FreeSpec with OneInstancePerTest with AuxSpecs {
  val semaphore = new Semaphore(0)
  "SimpleTypedActor" - {
    "basic test" in {
      val $ = new SimpleTypedActor[String, Int] {
        override protected def apply(m: String) = m.length
      } ! "Foobar"
      Await.result($, Duration.Inf) shouldReturn 6
    }
    "process requests in FIFO" in {
      val sb = new StringBuilder
      val semaphore = new Semaphore(0)
      val map = Map("1" -> new Semaphore(0), "2" -> new Semaphore(0))
      def appendToSb(i: Int) {
        sb append i.toString
        semaphore.release()
      }
      val $ = new SimpleTypedActor[String, Int] {
        override protected def apply(m: String) = {
          map(m).acquire()
          val $ = m.toInt
          appendToSb($)
          $
        }
      }

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
}
