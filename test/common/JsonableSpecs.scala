package common

import common.Jsonable.ToJsonableOps
import org.scalacheck.Arbitrary
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{PropSpec, Suite}

trait JsonableSpecs extends PropSpec with GeneratorDrivenPropertyChecks with AuxSpecs with ToJsonableOps {self: Suite =>
  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 10, workers = 5)
  def propJsonTest[T: Jsonable : Arbitrary: Manifest](): Unit = {
    property(manifest.runtimeClass.getCanonicalName + " Jsonable") {
      forAll {xs: T => jsonTest(xs)}
    }
  }
  def jsonTest[T: Jsonable](t: T): Unit = {
    t.jsonify.parse[T] shouldReturn t
  }
}
