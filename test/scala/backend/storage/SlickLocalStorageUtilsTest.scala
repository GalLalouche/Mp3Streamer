package backend.storage

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.{BeforeAndAfter, FreeSpec}

class SlickLocalStorageUtilsTest extends FreeSpec with AuxSpecs with BeforeAndAfter {
  val c = TestConfiguration
  import c._
  import c.driver.api._

  private class Rows(tag: Tag) extends Table[(String, String)](tag, "TABLE") {
    def key = column[String]("KEY", O.PrimaryKey)
    def value = column[String]("VALUE")
    def * = (key, value)
  }
  private val table = TableQuery[Rows]
  val $: LocalStorageUtils = SlickLocalStorageUtils(c)(table)
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
      $.createTable()
          .onEnd($.doesTableExist).get shouldReturn true
    }
    "no after drop" in {
      $.createTable()
          .onEnd($.dropTable())
          .onEnd($.doesTableExist).get shouldReturn false
    }
    "yes after clear" in {
      $.createTable()
          .onEnd($.clearTable())
          .onEnd($.doesTableExist).get shouldReturn true
    }
  }
  "create" - {
    "should succeed when table doesn't exist" in {
      $.createTable().get shouldReturn true
    }
    "should fail when table exists" in {
      $.createTable().get shouldReturn true
      $.createTable().get shouldReturn false
    }
    "succeed after drop" in {
      $.createTable()
          .onEnd($.dropTable())
          .onEnd($.createTable()).get shouldReturn true
    }
  }
  "clear" - {
    "when table does not exist returns false" in {
      $.clearTable().get shouldReturn false
    }
    "when table exists, returns true and crears the table" in {
      $.createTable().get
      c.db.run(table.+=("key" -> "value")).get
      c.db.run(table.result).get.toList shouldReturn List("key" -> "value")
      $.clearTable().get shouldReturn true
      c.db.run(table.result).get.toList shouldReturn Nil
    }
  }
  "drop" - {
    "should fail if table does not exist" in {
      $.dropTable().get shouldReturn false
    }
    "should succeed if table exists" in {
      $.createTable().onEnd($.dropTable()).get shouldReturn true
    }
  }
}
