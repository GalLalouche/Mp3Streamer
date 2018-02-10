package common

import monocle.Iso
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

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

  private case class StringWrapper(s: String)
  private implicit val jsonableWrapper: Jsonable[StringWrapper] = Jsonable.isoJsonable(Iso[StringWrapper, String](_.s)(StringWrapper))
  private implicit val arbStringWrapper: Arbitrary[StringWrapper] = Arbitrary(arbitrary[String].map(StringWrapper))
  propJsonTest[StringWrapper]()
}
