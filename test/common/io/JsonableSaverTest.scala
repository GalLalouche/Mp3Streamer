package common.io

import java.io.FileNotFoundException
import java.time.{LocalDateTime, ZoneOffset}

import common.AuxSpecs
import common.RichJson._
import common.json.{Jsonable, ToJsonableOps}
import common.rich.RichT._
import org.scalatest.{FreeSpec, OneInstancePerTest}
import play.api.libs.json.{JsObject, JsValue, Json}

class JsonableSaverTest extends FreeSpec with OneInstancePerTest with AuxSpecs with ToJsonableOps {
  private val root: DirectoryRef = new MemoryRoot
  private implicit val rootProvider: RootDirectoryProvider =
    new RootDirectoryProvider {
      override def rootDirectory: DirectoryRef = root
    }
  private val $ = new JsonableSaver()
  case class Person(age: Int, name: String)
  implicit object PersonJsonable extends Jsonable[Person] {
    override def jsonify(p: Person): JsObject = Json obj("age" -> p.age, "name" -> p.name)
    override def parse(json: JsValue): Person = Person(json int "age", json str "name")
  }
  val p1 = Person(1, "name1")
  val p2 = Person(2, "name2")
  val p3 = Person(3, "name3")
  "save" - {
    "can later load" in {
      $ save Seq(p1)
      $.loadArray.head shouldReturn p1
    }
    "overwrites previous save" in {
      $ save Seq(p1)
      $ save Seq(p2)
      $.loadArray.head shouldReturn p2
    }
    "object" - {
      "exists" in {
        $ save p1
        $.loadObject shouldReturn p1
      }
      "no previous file exists" in {
        a[FileNotFoundException] should be thrownBy $.loadObject
      }
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
      val persons: Seq[Person] = Seq(p1, p2, p3)
      $ save persons
      $.loadArray shouldReturn persons
    }
    "Classes that save as arrays can be loaded as objects" in {
      case class Persons(ps: Seq[Person])
      implicit val JsonablePersons: Jsonable[Persons] = new Jsonable[Persons] {
        override def jsonify(ps: Persons): JsValue = ps.ps.jsonify
        override def parse(json: JsValue): Persons = Persons(json.parse[Seq[Person]])
      }
      val persons = Persons(Seq(p1, p2, p3))
      $ save persons
      $.loadObject[Persons] shouldReturn persons
    }
  }
  "update" - {
    "saves data" in {
      $.update[Person](_ ++ Seq(p1))
      $.loadArray.head shouldReturn p1
    }
    "doesn't overwrite data" in {
      $ save Seq(p1, p2)
      $.update[Person](_ ++ Seq(p3))
      $.loadArray shouldReturn Seq(p1, p2, p3)
    }
  }
  "lastUpdateTime" - {
    "None before a save" in {
      $.lastUpdateTime shouldReturn None
    }
    "Time after change" in {
      def toMillis(ldt: LocalDateTime): Long = ldt.toEpochSecond(ZoneOffset.UTC)
      val now = LocalDateTime.now |> toMillis
      $ save p1
      val lastUpdateTime = $.lastUpdateTime[Person].get |> toMillis
      Math.abs(now - lastUpdateTime) < 10 shouldReturn true
    }
  }
  "override file name" in {
    val $ = new JsonableSaver {
      override protected def jsonFileName[T: Manifest] = "foobars.json"
    }
    $ save p1
    val files = root.deepFiles
    files.toList.map(_.name) shouldReturn List("foobars.json")
    $.loadObject shouldReturn p1
  }
}
