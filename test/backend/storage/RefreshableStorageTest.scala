package backend.storage

import java.time.Duration

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.RichT._
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.concurrent.Future

class RefreshableStorageTest extends FreeSpec with AuxSpecs with OneInstancePerTest {
  private implicit val c = new TestConfiguration
  implicit val clock = c.clock
  private var i = 0
  private val freshnessStorage = new FreshnessStorage[String, String](new MemoryBackedStorage)
  private val $ =
    new RefreshableStorage[String, String](freshnessStorage, e => {
      i += 1
      Future successful (e.reverse + i)
    }, Duration ofMillis 50)
  "apply" - {
    "no previous value should insert new value in" in {
      freshnessStorage.load("foobar").get shouldReturn None
      $("foobar").get shouldReturn "raboof1"
      freshnessStorage.load("foobar").get.get shouldReturn "raboof1"
    }
    "existing value is fresh should return existing value" in {
      freshnessStorage.store("foobar", "bazqux")
      $("foobar").get shouldReturn "bazqux"
      freshnessStorage.load("foobar").get.get shouldReturn "bazqux"
    }
    "existing value is stale should refresh" in {
      freshnessStorage.store("foobar", "bazqux")
      clock advance 100
      $("foobar").get shouldReturn "raboof1"
      freshnessStorage.load("foobar").get.get shouldReturn "raboof1"
      $("foobar").get shouldReturn "raboof1"
    }
    "existing value has no datetime" in {
      freshnessStorage.storeWithoutTimestamp("foobar", "bazqux")

      $("foobar").get shouldReturn "bazqux"
    }
    "reuse existing value on failure" - {
      "previous value exists" in {
        val $ = new RefreshableStorage[String, String](freshnessStorage,
          Future.failed(new RuntimeException()).const,
          Duration ofMillis 50)
        freshnessStorage.store("foo", "bar")
        val dataFreshness = freshnessStorage.freshness("foo").get
        clock advance 100
        assert($.needsRefresh("foo").get)
        $.apply("foo").get shouldReturn "bar"
        freshnessStorage.freshness("foo").get shouldReturn dataFreshness
      }
      "previous does not exist, returns the original failure" in {
        val exception = new RuntimeException("failure")
        val $ = new RefreshableStorage[String, String](freshnessStorage,
          Future.failed(exception).const,
          Duration ofMillis 50)
        $.apply("foo").getFailure shouldReturn exception
      }
    }
  }
  "withAge" in {
    freshnessStorage.store("foobar", "bazqux")
    val timestamp = freshnessStorage.freshness("foobar").get.get.get // Yeah :|
    $.withAge("foobar").get shouldReturn ("bazqux" -> Some(timestamp))
  }
}
