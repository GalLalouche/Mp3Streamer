package common.io

import java.io.FileNotFoundException
import java.time.{LocalDateTime, ZoneOffset}

import common.rich.RichT._
import common.{AuxSpecs, Jsonable}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}
import play.api.libs.json.{JsObject, Json}

class JsonableSaverTest extends FreeSpec with OneInstancePerTest with MockitoSugar with AuxSpecs {
  private implicit val root = new MemoryRoot
  private val $ = new JsonableSaver
  case class Person(age: Int, name: String)
  implicit object PersonJsonable extends Jsonable[Person] {
    override def jsonify(p: Person): JsObject = Json obj("age" -> p.age, "name" -> p.name)
    override def parse(json: JsObject): Person = Person(json.\("age").as[Int], json.\("name").as[String])
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
}
