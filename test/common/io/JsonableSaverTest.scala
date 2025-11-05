package common.io

import java.io.FileNotFoundException
import java.time.{LocalDateTime, ZoneOffset}

import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json.{JsObject, Json, JsValue}

import common.json.Jsonable
import common.json.RichJson._
import common.json.ToJsonableOps._
import common.rich.RichT._
import common.test.AuxSpecs

private object JsonableSaverTest {
  private case class Person(age: Int, name: String)
  private implicit object JsonableEv extends Jsonable[Person] {
    override def jsonify(p: Person): JsObject = Json.obj("age" -> p.age, "name" -> p.name)
    override def parse(json: JsValue): Person = Person(json.int("age"), json.str("name"))
  }
}
class JsonableSaverTest extends AnyFreeSpec with OneInstancePerTest with AuxSpecs {
  import common.io.JsonableSaverTest._

  private val c = TestModuleConfiguration()
  private val root = c.injector.instance[MemoryRoot]

  private val $ = c.injector.instance[JsonableSaver]
  private val p1 = Person(1, "name1")
  private val p2 = Person(2, "name2")
  private val p3 = Person(3, "name3")
  "saveArray" - {
    "can later load" in {
      $.saveArray(Vector(p1))
      $.loadArray.head shouldReturn p1
    }
    "overwrites previous save" in {
      $.saveArray(Vector(p1))
      $.saveArray(Vector(p2))
      $.loadArray.head shouldReturn p2
    }
  }
  "saveObject" - {
    "exists" in {
      $.saveObject(p1)
      $.loadObject shouldReturn p1
    }
    "no previous file exists" in {
      a[FileNotFoundException] should be thrownBy $.loadObject
    }
  }
  "load" - {
    "is empty by default" in {
      $.loadArray[Person] shouldBe empty
    }
    "doesn't create a file if one doesn't exist" in {
      $.loadArray[Person]
      root.files shouldBe empty
    }
    "in order saved" in {
      val persons: Seq[Person] = Vector(p1, p2, p3)
      $.saveArray(persons)
      $.loadArray shouldReturn persons
    }
    "Classes containing arrays can be loaded as objects" in {
      case class Persons(ps: Seq[Person])
      implicit val JsonablePersons: Jsonable[Persons] = new Jsonable[Persons] {
        override def jsonify(ps: Persons): JsValue = ps.ps.jsonify
        override def parse(json: JsValue): Persons = Persons(json.parse[Seq[Person]])
      }
      val persons = Persons(Vector(p1, p2, p3))
      $.saveObject(persons)
      $.loadObject[Persons] shouldReturn persons
    }
  }
  "update" - {
    "saves data" in {
      $.update[Person](_ ++ Vector(p1))
      $.loadArray.head shouldReturn p1
    }
    "doesn't overwrite data" in {
      $.saveArray(Vector(p1, p2))
      $.update[Person](_ ++ Vector(p3))
      $.loadArray shouldReturn Vector(p1, p2, p3)
    }
  }
  "lastUpdateTime" - {
    "None before a save" in {
      $.lastUpdateTime shouldReturn None
    }
    "Time after change" in {
      def toMillis(ldt: LocalDateTime): Long = ldt.toEpochSecond(ZoneOffset.UTC)
      val now = LocalDateTime.now |> toMillis
      $.saveObject(p1)
      val lastUpdateTime = $.lastUpdateTime[Person].get |> toMillis
      Math.abs(now - lastUpdateTime) < 10 shouldReturn true
    }
  }
  "override file name" in {
    val $ = new JsonableSaver(c.injector.instance[DirectoryRef, RootDirectory]) {
      protected override def jsonFileName[T: Manifest] = "foobars.json"
    }
    $.saveObject(p1)
    val files = root.deepFiles
    files.toVector.map(_.name) shouldReturn Vector("foobars.json")
    $.loadObject shouldReturn p1
  }
}
