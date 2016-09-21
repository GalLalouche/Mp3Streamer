package search
import common.io.{JsonableSaver, MemoryRoot}
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
      $.save(Seq(p1))
      $.load.head shouldReturn p1
    }
    "overwrites previous save" in {
      $.save(Seq(p1))
      $.save(Seq(p2))
      $.load.head shouldReturn p2
    }
  }
  "load" - {
    "is empty by default" in {
      $.load[Person] shouldBe empty
    }
    "doesn't create a file if one doesn't exist" in {
      $.load[Person]
      root.files shouldBe empty
    }
    "in order saved" in {
      val persons: Seq[Person] = Seq(p1, p2, p3)
      $.save(persons)
      $.load shouldReturn persons
    }
  }
  "update" - {
    "saves data" in {
      $.update[Person](_ ++ Seq(p1))
      $.load.head shouldReturn p1
    }
    "doesn't overwrite data" in {
      $.save(Seq(p1, p2))
      $.update[Person](_ ++ Seq(p3))
      $.load shouldReturn Seq(p1, p2, p3)
    }
  }
}
