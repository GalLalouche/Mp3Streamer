package common

class JsonableTest extends JsonableSpecs {
  propJsonTest[Seq[Int]]()
  property("Recursive sequence") {
    forAll {(xs: Seq[Int], ys: Seq[Int]) => jsonTest(Seq(xs, ys))}
  }
  property("None") {
    jsonTest(Option.apply[String](null))
  }
  propJsonTest[Option[String]]()
  propJsonTest[(String, Int)]()
}
