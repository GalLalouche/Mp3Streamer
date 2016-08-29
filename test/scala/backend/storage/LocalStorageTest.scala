package backend.storage

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.collection.mutable
import scala.concurrent.Future



class LocalStorageTest extends FreeSpec with MockitoSugar with OneInstancePerTest with AuxSpecs {
  private implicit val c = new TestConfiguration
  private val existingValues = mutable.HashMap[Int, Int]()
  private val $ = new LocalStorageTemplate[Int, Int]() {
    override protected def internalForceStore(k: Int, v: Int): Future[Unit] = {
      existingValues += k -> v
      Future successful Unit
    }
    override def load(k: Int): Future[Option[Int]] = Future successful existingValues.get(k)
    override def utils = ???
  }
  "store" - {
    "has existing value returns false" in {
      existingValues += 1 -> 2
      $.store(1, 4).get shouldReturn false
      existingValues(1) shouldReturn 2
    }
    "no existing value should insert the value and return true" in {
      $.store(1, 4).get shouldReturn true
      existingValues(1) shouldReturn 4
    }
  }
  "forceStore" - {
    "has existing value returns old value" in {
      existingValues += 1 -> 2
      $.forceStore(1, 4).get shouldReturn Some(2)
      existingValues(1) shouldReturn 4
    }
    "has no existing value returns None" in {
      $.forceStore(1, 4).get shouldReturn None
      existingValues(1) shouldReturn 4
    }
  }
}
