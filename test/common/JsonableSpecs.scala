package common

import org.scalacheck.{Arbitrary, Shrink}
import org.scalatest.Suite
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import common.json.Jsonable
import common.json.ToJsonableOps._
import common.test.AuxSpecs

trait JsonableSpecs extends AnyPropSpec with ScalaCheckDrivenPropertyChecks with AuxSpecs {
  self: Suite =>
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 100, workers = 5)
  def propJsonTest[T: Jsonable: Arbitrary: Manifest](): Unit =
    property(manifest.runtimeClass.getCanonicalName + " Jsonable") {
      forAll { xs: T => jsonTest(xs) }
    }
  def propJsonTest2[T: Jsonable: Arbitrary: Shrink: Manifest](): Unit =
    property(manifest.runtimeClass.getCanonicalName + " Jsonable") {
      forAll { xs: T => jsonTest(xs) }
    }
  def jsonTest[T: Jsonable](t: T): Unit = {
    if (t.jsonify.parse[T] != t) {
      println(t)
      println(t.jsonify.parse[T])
    }
    t.jsonify.parse[T] shouldReturn t
  }
}
