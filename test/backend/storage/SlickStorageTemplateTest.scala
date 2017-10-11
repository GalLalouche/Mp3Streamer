package backend.storage

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.{BeforeAndAfter, FreeSpec}
import slick.ast.{BaseTypedType, ScalaBaseType}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class SlickStorageTemplateTest extends FreeSpec with AuxSpecs with BeforeAndAfter
    with ToBindOps with FutureInstances {
  private implicit val c = new TestConfiguration
  private val table = new TestTable
  before {
    table.utils.clearOrCreateTable().get
  }
  "storage" - {
    "store" - {
      "stores when new value" in {
        table.store(1, "foo").get
        table.store(2, "bar").get
        table.load(1).get.get shouldReturn "foo"
        table.load(2).get.get shouldReturn "bar"
      }
      "throws when value already exists" in {
        table.store(1, "foo").get
        an[Exception] should be thrownBy table.store(1, "bar").get
      }
    }
    "store multiples" - {
      "stores when new value" in {
        table.storeMultiple(List((1, "foo"), (2, "bar"))).get
        table.load(1).get.get shouldReturn "foo"
        table.load(2).get.get shouldReturn "bar"
      }
      "throws when value already exists" in {
        table.store(1, "foo").get
        an[Exception] should be thrownBy table.storeMultiple(List((2, "bar"), (1, "moo"))).get
      }
    }
    "forceStore" - {
      "returns None when no previous value existed" in {
        table.store(1, "foo").get
        table.forceStore(2, "bar").get shouldReturn None
        table.load(1).get.get shouldReturn "foo"
        table.load(2).get.get shouldReturn "bar"
      }
      "returns previous value when it existed" in {
        table.store(1, "foo").get
        table.load(1).get.get shouldReturn "foo"
        table.forceStore(1, "bar").get shouldReturn Some("foo")
        table.load(1).get.get shouldReturn "bar"
      }
    }
    "delete" - {
      "does nothing and returns None when no previous value" in {
        table.store(1, "foo").get
        table.delete(2).get shouldReturn None
        table.load(1).get.get shouldReturn "foo"
      }
      "returns previous value and deletes when it existed" in {
        table.store(1, "foo").get
        table.store(2, "bar").get
        table.delete(2).get shouldReturn Some("bar")
        table.load(1).get.get shouldReturn "foo"
        table.load(2).get shouldReturn None
      }
    }
  }
}
