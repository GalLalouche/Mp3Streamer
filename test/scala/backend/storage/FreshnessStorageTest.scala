package backend.storage

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.joda.time.DateTime
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class FreshnessStorageTest extends FreeSpec with AuxSpecs with OneInstancePerTest
    with FutureInstances with ToBindOps {
  private implicit val c = TestConfiguration()
  private var now = new DateTime(0)
  private val $ = new FreshnessStorage[Int, Int](new MemoryBackedStorage) {
    override protected def now = FreshnessStorageTest.this.now
  }

  "store and load" - {
    "Can load stored data" in {
      $.store(1, 2).>>($ load 1).get.get shouldReturn 2
    }
    "Returns none if no data" in {
      $.load(1).get shouldReturn None
    }
  }
  "freshness" - {
    "no existing data" in {
      $.freshness(1).get shouldReturn None
    }
    "existing data but no timestamp" in {
      $.storeWithoutTimestamp(1, 2).>>($ freshness 1).get.get shouldReturn None
    }
    "existing data with timestamp" in {
      $.store(1, 2).>>($ freshness 1).get.get.get shouldReturn now
    }
  }
  "mapStore updates timestamp" in {
    $.store(1, 2).get
    now = new DateTime(1)
    $.freshness(1).get.get.get shouldReturn new DateTime(0)

    $.mapStore(1, _ * 2, ???).get.get shouldReturn 2
    $.load(1).get.get shouldReturn 4
    $.freshness(1).get.get.get shouldReturn new DateTime(1)
  }
}
