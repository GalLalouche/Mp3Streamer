package common

class JsonableTest extends JsonableSpecs {
  "derived instances" - {
    "Seq" - {
      "empty" in jsonTest(Seq[Int]())
      "with values" in jsonTest(Seq(1, 2, 3, 4))
      "recursive" in jsonTest(Seq(Seq(1, 2), Seq(3, 4)))
    }
    "Option" - {
      "None" in jsonTest(Option.apply[String](null))
      "Some" in jsonTest[Option[String]](Some("foo"))
    }
    "Pair" in jsonTest(4 -> "foo")
  }
}
