package common

import org.scalatest.{PropSpec, Suite}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import common.json.Jsonable
import common.json.ToJsonableOps._
import common.test.AuxSpecs
import org.scalacheck.Arbitrary

trait JsonableSpecs extends PropSpec with GeneratorDrivenPropertyChecks with AuxSpecs {
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
