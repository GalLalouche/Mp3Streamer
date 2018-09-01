package backend.storage

import java.time.Clock

import backend.RichTime._
import backend.configs.TestModuleConfiguration
import common.{AuxSpecs, FakeClock}
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.concurrent.ExecutionContext

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class FreshnessStorageTest extends FreeSpec with AuxSpecs with OneInstancePerTest
    with FutureInstances with ToBindOps {
  private val c = TestModuleConfiguration()
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val clock: FakeClock = c.injector.instance[FakeClock]

  private val $ = new FreshnessStorage[Int, Int](new MemoryBackedStorage, c.injector.instance[Clock])

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
      $.store(1, 2).>>($ freshness 1).get.get.get shouldReturn clock.instant.toLocalDateTime
    }
  }
  "mapStore updates timestamp" in {
    $.store(1, 2).get
    clock advance 1
    $.freshness(1).get.get.get shouldReturn 0.toLocalDateTime

    $.mapStore(1, _ * 2, ???).get.get shouldReturn 2
    $.load(1).get.get shouldReturn 4
    $.freshness(1).get.get.get shouldReturn 1.toLocalDateTime
  }
}
