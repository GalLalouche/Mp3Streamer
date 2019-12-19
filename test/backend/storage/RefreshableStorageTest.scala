package backend.storage

import java.time.{Clock, Duration}

import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}
import org.scalatest.OptionValues._

import scala.concurrent.Future

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind._

import common.{AsyncAuxSpecs, FakeClock}
import common.rich.RichT._

class RefreshableStorageTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val c = new TestModuleConfiguration
  private val clock = c.injector.instance[FakeClock]
  private var i = 0
  private val freshnessStorage = new FreshnessStorage[String, String](
    new MemoryBackedStorage, c.injector.instance[Clock])
  private val $ = new RefreshableStorage[String, String](
    freshnessStorage,
    e => {
      i += 1
      Future successful (e.reverse + i)
    },
    Duration ofMillis 50,
    c.injector.instance[Clock]
  )
  "apply" - {
    "no previous value should insert new value in" in {
      freshnessStorage.load("foobar").map(_ shouldReturn None) >>
          $("foobar").map(_ shouldReturn "raboof1") >>
          freshnessStorage.load("foobar").map(_.value shouldReturn "raboof1")
    }
    "existing value is fresh should return existing value" in {
      freshnessStorage.store("foobar", "bazqux") >>
          $("foobar").map(_ shouldReturn "bazqux") >>
          freshnessStorage.load("foobar").map(_.value shouldReturn "bazqux")
    }
    "existing value is stale should refresh" in {
      freshnessStorage.store("foobar", "bazqux") >|
          clock.advance(100) >>
          $("foobar").map(_ shouldReturn "raboof1") >>
          freshnessStorage.load("foobar").map(_.value shouldReturn "raboof1") >>
          $("foobar").map(_ shouldReturn "raboof1")
    }
    "existing value has no datetime" in {
      freshnessStorage.storeWithoutTimestamp("foobar", "bazqux") >> $("foobar").map(_ shouldReturn "bazqux")
    }
    "reuse existing value on failure" - {
      "previous value exists" in {
        val $ = new RefreshableStorage[String, String](freshnessStorage,
          Future.failed(new RuntimeException()).const,
          Duration ofMillis 50,
          c.injector.instance[Clock],
        )
        for {
          _ <- freshnessStorage.store("foo", "bar")
          dataFreshness <- freshnessStorage.freshness("foo")
          _ = clock advance 100
          a <- $.needsRefresh("foo").map(_ shouldReturn true) >>
              $.apply("foo").map(_ shouldReturn "bar") >>
              freshnessStorage.freshness("foo").map(_ shouldReturn dataFreshness)
        } yield a
      }
      "previous does not exist, returns the original failure" in {
        val exception = new RuntimeException("failure")
        val $ = new RefreshableStorage[String, String](freshnessStorage,
          Future.failed(exception).const,
          Duration ofMillis 50,
          c.injector.instance[Clock],
        )
        $("foo").checkFailure(_ shouldReturn exception)
      }
    }
  }
  "withAge" in {
    for {
      _ <- freshnessStorage.store("foobar", "bazqux")
      timestamp <- freshnessStorage.freshness("foobar")
      ld = timestamp.value.localDateTime.value
      age <- $.withAge("foobar")
    } yield age shouldReturn ("bazqux" -> DatedFreshness(ld))
  }
}
