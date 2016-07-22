//package backend.storage
//
//import common.AuxSpecs
//import common.RichFuture._
//import common.concurrency.SingleThreadedExecutionContext._
//import org.scalatest.mock.MockitoSugar
//import org.scalatest.{FreeSpec, OneInstancePerTest}
//
//import scala.collection.mutable
//import scala.concurrent.Future
//
//
//
//class NewLocalStorageTest extends FreeSpec with MockitoSugar with OneInstancePerTest with AuxSpecs {
//  private val existingValues = mutable.HashMap[Int, Int]()
//  private val $ = new NewLocalStorage[Int, Int]() {
//    override protected def internalForceStore(k: Int, v: Int): Future[Unit] = {
//      existingValues += k -> v
//      Future successful Unit
//    }
//    override def newLoad(k: Int): Future[Option[Int]] = Future successful existingValues.get(k)
//  }
//  "store" - {
//    "has existing value returns false" in {
//      existingValues += 1 -> 2
//      $.newStore(1, 4).get shouldReturn false
//      existingValues(1) shouldReturn 2
//    }
//    "no existing value should insert the value and return true" in {
//      $.newStore(1, 4).get shouldReturn true
//      existingValues(1) shouldReturn 4
//    }
//  }
//  "forceStore" - {
//    "has existing value returns old value" in {
//      existingValues += 1 -> 2
//      $.forceStore(1, 4).get shouldReturn Some(2)
//      existingValues(1) shouldReturn 4
//    }
//    "has no existing value returns None" in {
//      $.forceStore(1, 4).get shouldReturn None
//      existingValues(1) shouldReturn 4
//    }
//  }
//}
