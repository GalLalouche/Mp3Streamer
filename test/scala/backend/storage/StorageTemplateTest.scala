package backend.storage

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.collection.mutable
import scala.concurrent.Future


class StorageTemplateTest extends FreeSpec with MockitoSugar with OneInstancePerTest with AuxSpecs {
  private implicit val c = new TestConfiguration
  private val existingValues = mutable.HashMap[Int, Int]()
  private val $ = new StorageTemplate[Int, Int]() {
    override protected def internalForceStore(k: Int, v: Int) = {
      existingValues += k -> v
      Future successful Unit
    }
    override def load(k: Int): Future[Option[Int]] = Future successful existingValues.get(k)
    override protected def internalDelete(k: Int) = Future successful existingValues.remove(k)
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
  "mapStore" - {
    "has no existing value uses default" in {
      $.mapStore(1, e => ???, 2).get shouldReturn None
      existingValues(1) shouldReturn 2
    }
    "maps existign value if present" in {
      existingValues += 1 -> 2
      $.mapStore(1, _ * 2, ???).get shouldReturn Option(2)
      existingValues(1) shouldReturn 4
    }
  }
  "delete" - {
    "existing value" in {
      existingValues += 1 -> 2
      $.delete(1).get.get shouldReturn 2
      $.load(1).get shouldReturn None
    }
    "no existing value" in {
      $.delete(1).get shouldReturn None
      $.load(1).get shouldReturn None
    }
  }
}
