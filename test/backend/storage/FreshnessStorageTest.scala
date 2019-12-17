package backend.storage

import java.time.Clock

import backend.RichTime._
import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}
import org.scalatest.OptionValues._

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind._

import common.{AuxSpecs, FakeClock}

class FreshnessStorageTest extends AsyncFreeSpec with AuxSpecs with OneInstancePerTest {
  private val c = TestModuleConfiguration()
  private val clock: FakeClock = c.injector.instance[FakeClock]

  private val $ = new FreshnessStorage[Int, Int](new MemoryBackedStorage, c.injector.instance[Clock])

  "store and load" - {
    "Can load stored data" in {
      $.store(1, 2) >> $.load(1).map(_.value shouldReturn 2)
    }
    "Returns none if no data" in {
      $.load(1).map(_ shouldReturn None)
    }
  }
  "freshness" - {
    "no existing data" in {
      $.freshness(1).map(_ shouldReturn None)
    }
    "existing data but no timestamp" in {
      $.storeWithoutTimestamp(1, 2) >> $.freshness(1).map(_.value shouldReturn AlwaysFresh)
    }
    "existing data with timestamp" in {
      $.store(1, 2) >>
          $.freshness(1).map(_.value.localDateTime.value shouldReturn clock.instant().toLocalDateTime)
    }
  }
  "mapStore updates timestamp" in {
    $.store(1, 2) >|
        clock.advance(1) >>
        $.freshness(1).map(_.value.localDateTime.value shouldReturn 0.toLocalDateTime) >>
        $.mapStore(1, _ * 2, ???).map(_.value shouldReturn 2) >>
        $.load(1).map(_.value shouldReturn 4) >>
        $.freshness(1).map(_.value.localDateTime.value shouldReturn 1.toLocalDateTime)
  }
}
