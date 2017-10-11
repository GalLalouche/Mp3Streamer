package backend.storage

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.{BeforeAndAfter, FreeSpec}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class SlickTableUtilsTest extends FreeSpec with AuxSpecs with BeforeAndAfter
    with ToBindOps with FutureInstances {
  private implicit val c = new TestConfiguration
  private val table = new TestTable
  private val $ = table.utils
  before {
    $.dropTable().get
  }
  after {
    $.dropTable().get
  }
  "utils" - {
    val $ = table.utils
    "does table exists" - {
      "no by default" in {
        $.doesTableExist.get shouldReturn false
      }
      "yes after creation" in {
        $.createTable().>>($.doesTableExist).get shouldReturn true
      }
      "no after drop" in {
        $.createTable()
            .>>($.dropTable())
            .>>($.doesTableExist).get shouldReturn false
      }
      "yes after clear" in {
        $.createTable()
            .>>($.clearTable())
            .>>($.doesTableExist).get shouldReturn true
      }
    }
    "create" - {
      "should not throw when table doesn't exist" in {
        $.createTable().get
      }
      "should fail when table exists" in {
        $.createTable().get
        an[Exception] should be thrownBy $.createTable().get
      }
      "succeed after drop" in {
        $.createTable()
            .>>($.dropTable())
            .>>($.createTable()).get
      }
    }
    "clear" - {
      "when table throws" in {
        an[Exception] should be thrownBy $.clearTable().get
      }
      "when table exists, clears the table" in {
        $.createTable().get
        table.store(2, "foo").get
        table.load(2).get.get shouldReturn "foo"
        $.clearTable().get
        table.load(2).get shouldReturn None
      }
    }
    "drop" - {
      "should fail if table does not exist" in {
        $.dropTable().get shouldReturn false
      }
      "should succeed if table exists" in {
        $.createTable().>>($.dropTable()).get shouldReturn true
      }
    }
  }
}
