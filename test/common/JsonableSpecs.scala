package common

import org.scalacheck.Arbitrary
import org.scalatest.{PropSpec, Suite}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import common.json.Jsonable
import common.json.ToJsonableOps._
import common.test.AuxSpecs

trait JsonableSpecs extends PropSpec with ScalaCheckDrivenPropertyChecks with AuxSpecs {
  self: Suite =>
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 10, workers = 5)
  def propJsonTest[T: Jsonable: Arbitrary: Manifest](): Unit =
    property(manifest.runtimeClass.getCanonicalName + " Jsonable") {
      forAll { xs: T => jsonTest(xs) }
    }
  def jsonTest[T: Jsonable](t: T): Unit =
    t.jsonify.parse[T] shouldReturn t
}
