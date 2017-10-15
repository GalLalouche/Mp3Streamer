package common

import common.Jsonable.ToJsonableOps
import org.scalatest.FreeSpec

class JsonableTest extends FreeSpec with AuxSpecs with ToJsonableOps {
  private def test[T: Jsonable](t: T): Unit = {
    t.jsonify.parse[T] shouldReturn t
  }
  "primitives" - {
    "int" in test(4)
    "string" in test("foo")
  }
  "derived instances" - {
    "Seq" - {
      "empty" in test(Seq[Int]())
      "with values" in test(Seq(1, 2, 3, 4))
      "recursive" in test(Seq(Seq(1, 2), Seq(3, 4)))
    }
    "Option" - {
      "None" in test(Option.apply[String](null))
      "Some" in test[Option[String]](Some("foo"))
    }
    "Pair" in test(4 -> "foo")
  }
}
