package backend.storage

import java.time.Clock

import backend.RichTime._
import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}
import org.scalatest.OptionValues._

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind._

import common.FakeClock
import common.test.AsyncAuxSpecs

class FreshnessStorageTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val c = TestModuleConfiguration()
  private val clock: FakeClock = c.injector.instance[FakeClock]

  private val $ = new FreshnessStorage[Int, Int](new MemoryBackedStorage, c.injector.instance[Clock])

  "store and load" - {
    "Can load stored data" in {
      $.store(1, 2) >> $.load(1).mapValue(_ shouldReturn 2)
    }
    "Returns none if no data" in {
      $.load(1).shouldEventuallyReturnNone()
    }
  }
  "freshness" - {
    "no existing data" in {
      $.freshness(1).shouldEventuallyReturnNone()
    }
    "existing data but no timestamp" in {
      $.storeWithoutTimestamp(1, 2) >> $.freshness(1).mapValue(_ shouldReturn AlwaysFresh)
    }
    "existing data with timestamp" in {
      $.store(1, 2) >>
          $.freshness(1).mapValue(_.localDateTime.value shouldReturn clock.instant().toLocalDateTime)
    }
  }
  "mapStore updates timestamp" in {
    $.store(1, 2) >| clock.advance(1) >> checkAll(
      $.freshness(1).mapValue(_.localDateTime.value shouldReturn 0.toLocalDateTime),
      $.mapStore(1, _ * 2, ???).mapValue(_ shouldReturn 2),
      $.load(1).mapValue(_ shouldReturn 4),
      $.freshness(1).mapValue(_.localDateTime.value shouldReturn 1.toLocalDateTime),
    )
  }
}
