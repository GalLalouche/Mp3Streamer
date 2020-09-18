package backend.storage

import java.time.Clock

import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}
import org.scalatest.OptionValues._

import scalaz.syntax.bind.ToBindOps
import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._

import common.FakeClock
import common.rich.RichTime.{RichInstant, RichLong}
import common.storage.StoreMode
import common.test.AsyncAuxSpecs

class ComposedFreshnessStorageTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val c = TestModuleConfiguration()
  private val clock: FakeClock = c.injector.instance[FakeClock]

  private val $ = new ComposedFreshnessStorage[Int, Int](new MemoryBackedStorage, c.injector.instance[Clock])

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
      $.mapStore(StoreMode.Update, 1, _ * 2, ???).mapValue(_ shouldReturn 2),
      $.load(1).mapValue(_ shouldReturn 4),
      $.freshness(1).mapValue(_.localDateTime.value shouldReturn 1.toLocalDateTime),
    )
  }
}
