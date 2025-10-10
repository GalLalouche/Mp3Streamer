package common

import common.json.Jsonable
import monocle.Iso
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.{JsNumber, JsString, JsValue}

class JsonableTest extends JsonableSpecs {
  private implicit object IntJsonable extends Jsonable[Int] {
    override def jsonify(i: Int): JsValue = JsNumber(i)
    override def parse(json: JsValue): Int = json.asInstanceOf[JsNumber].value.intValue
  }
  private implicit object StringJsonable extends Jsonable[String] {
    override def jsonify(s: String): JsValue = JsString(s)
    override def parse(json: JsValue): String = json.asInstanceOf[JsString].value
  }
  propJsonTest[Seq[Int]]()
  property("Recursive sequence") {
    forAll((xs: Seq[Int], ys: Seq[Int]) => jsonTest(Vector(xs, ys)))
  }
  property("None") {
    jsonTest(Option.apply[String](null))
  }
  propJsonTest[Option[String]]()
  propJsonTest[(String, Int)]()

  private case class StringWrapper(s: String)
  private implicit val jsonableWrapper: Jsonable[StringWrapper] =
    Jsonable.isoJsonable(Iso[StringWrapper, String](_.s)(StringWrapper))
  private implicit val arbStringWrapper: Arbitrary[StringWrapper] =
    Arbitrary(arbitrary[String].map(StringWrapper))
  propJsonTest[StringWrapper]()
}
