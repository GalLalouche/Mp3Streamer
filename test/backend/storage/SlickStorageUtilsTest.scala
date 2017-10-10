package backend.storage

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.{BeforeAndAfter, FreeSpec}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class SlickStorageUtilsTest extends FreeSpec with AuxSpecs with BeforeAndAfter
    with ToBindOps with FutureInstances {
  val c = new TestConfiguration
  import c._
  import c.profile.api._

  private class Rows(tag: Tag) extends Table[(String, String)](tag, "TABLE") {
    def key = column[String]("KEY", O.PrimaryKey)
    def value = column[String]("VALUE")
    def * = (key, value)
  }
  private val table = TableQuery[Rows]
  val $ = SlickStorageUtils(c)(table)
  before {
    $.dropTable().get
  }
  after {
    $.dropTable().get
  }
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
    "when table exists, returns true and crears the table" in {
      $.createTable().get
      c.db.run(table.+=("key" -> "value")).get
      c.db.run(table.result).get.toList shouldReturn List("key" -> "value")
      $.clearTable().get
      c.db.run(table.result).get.toList shouldReturn Nil
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
