package search
import common.io.Root
import org.scalatest.{FreeSpec, ShouldMatchers, OneInstancePerTest}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{Json, JsObject}

class JsonableSaverTest extends FreeSpec with ShouldMatchers with OneInstancePerTest with MockitoSugar {
  val root = new Root
  private val $ = new JsonableSaver(root)
  case class Person(age: Int, name: String)
  implicit object PersonJsonable extends Jsonable[Person] {
    override def jsonify(p: Person): JsObject = Json obj("age" -> p.age, "name" -> p.name)
    override def parse(json: JsObject): Person = Person(json.\("age").as[Int], json.\("name").as[String])
  }
  val p1 = new Person(1, "name1")
  val p2 = new Person(2, "name2")
  val p3 = new Person(3, "name3")
  "save" - {
    "can later load" in {
      $.save(Seq(p1))
      $.load.head should be === p1
    }
    "overwrites previous save" in {
      $.save(Seq(p1))
      $.save(Seq(p2))
      $.load.head should be === p2
    }
  }
  "load" - {
    "is empty by default" in {
      $.load[Person].isEmpty should be === true
    }
    "doesn't create a file if one doesn't exist" in {
      $.load[Person]
      root.files.isEmpty should be === true
    }
    "in order saved" in {
      val persons: Seq[Person] = Seq(p1, p2, p3)
      $.save(persons)
      $.load should be === persons
    }
  }
  "update" - {
    "saves data" in {
      $.update[Person](_ ++ Seq(p1))
      $.load.head should be === p1
    }
    "doesn't overwrite data" in {
      $.save(Seq(p1, p2))
      $.update[Person](_ ++ Seq(p3))
      $.load should be === Seq(p1, p2, p3)
    }
  }
}
