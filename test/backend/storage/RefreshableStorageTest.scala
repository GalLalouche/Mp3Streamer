package backend.storage

import java.time.{Clock, Duration}

import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.OneInstancePerTest
import org.scalatest.OptionValues._
import org.scalatest.freespec.AsyncFreeSpec

import scala.concurrent.Future

import cats.implicits.catsSyntaxFlatMapOps
import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.FakeClock
import common.rich.RichT._
import common.test.AsyncAuxSpecs

class RefreshableStorageTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val c = new TestModuleConfiguration
  private val clock = c.injector.instance[FakeClock]
  private var i = 0
  private val freshnessStorage = new ComposedFreshnessStorage[String, String](
    new MemoryBackedStorage,
    c.injector.instance[Clock],
  )
  private val $ = new RefreshableRetriever[String, String](
    freshnessStorage,
    e => {
      i += 1
      Future.successful(e.reverse + i)
    },
    Duration.ofMillis(50),
    c.injector.instance[Clock],
  )
  "apply" - {
    "no previous value should insert new value in" in {
      freshnessStorage.load("foobar").shouldEventuallyReturnNone() >> checkAll(
        $("foobar") shouldEventuallyReturn "raboof1",
        freshnessStorage.load("foobar").valueShouldEventuallyReturn("raboof1"),
      )
    }
    "existing value is fresh should return existing value" in {
      freshnessStorage.store("foobar", "bazqux") >> checkAll(
        $("foobar") shouldEventuallyReturn "bazqux",
        freshnessStorage.load("foobar").valueShouldEventuallyReturn("bazqux"),
      )
    }
    "existing value is stale should refresh" in {
      freshnessStorage.store("foobar", "bazqux") >| clock.advance(100) >> checkAll(
        $("foobar") shouldEventuallyReturn "raboof1",
        freshnessStorage.load("foobar").valueShouldEventuallyReturn("raboof1"),
        $("foobar") shouldEventuallyReturn "raboof1",
      )
    }
    "existing value has no datetime" in {
      freshnessStorage.storeWithoutTimestamp("foobar", "bazqux") >>
        $("foobar").shouldEventuallyReturn("bazqux")
    }
    "reuse existing value on failure" - {
      "previous value exists" in {
        val $ = new RefreshableRetriever[String, String](
          freshnessStorage,
          Future.failed(new RuntimeException()).const,
          Duration.ofMillis(50),
          c.injector.instance[Clock],
        )
        for {
          _ <- freshnessStorage.store("foo", "bar")
          dataFreshness <- freshnessStorage.freshness("foo").value
          _ = clock.advance(100)
          a <- checkAll(
            $.needsRefresh("foo") shouldEventuallyReturn true,
            $.apply("foo") shouldEventuallyReturn "bar",
            freshnessStorage.freshness("foo").value shouldEventuallyReturn dataFreshness,
          )
        } yield a
      }
      "previous does not exist, returns the original failure" in {
        val exception = new RuntimeException("failure")
        val $ = new RefreshableRetriever[String, String](
          freshnessStorage,
          Future.failed(exception).const,
          Duration.ofMillis(50),
          c.injector.instance[Clock],
        )
        $("foo").failureShouldEventuallyReturn(exception)
      }
    }
  }
  "withAge" in {
    for {
      _ <- freshnessStorage.store("foobar", "bazqux")
      timestamp <- freshnessStorage.freshness("foobar").value
      ld = timestamp.value.localDateTime.value
      age <- $.withAge("foobar")
    } yield age shouldReturn ("bazqux" -> DatedFreshness(ld))
  }
}
