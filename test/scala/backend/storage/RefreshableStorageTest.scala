package backend.storage

import backend.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.joda.time.Duration
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.concurrent.Future

class RefreshableStorageTest extends FreeSpec with MockitoSugar with AuxSpecs with OneInstancePerTest {
  private implicit val c = TestConfiguration
  var i = 0
  val freshnessStorage = new FreshnessStorage[String, String](new MemoryBackedLocalStorage)
  val $ =
    new RefreshableStorage[String, String](freshnessStorage, e => {
      i += 1
      Future successful (e.reverse + i)
    }, Duration.millis(50))
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
      Thread sleep 100
      $("foobar").get shouldReturn "raboof1"
      freshnessStorage.load("foobar").get.get shouldReturn "raboof1"
      $("foobar").get shouldReturn "raboof1"
    }
    "existing value has no datetime" in {
      freshnessStorage.storeWithoutTimestamp("foobar", "bazqux")
      Thread sleep 100
      $("foobar").get shouldReturn "bazqux"
    }
  }
}
