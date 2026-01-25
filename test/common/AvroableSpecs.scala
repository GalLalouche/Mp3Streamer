package common

import org.scalacheck.Arbitrary
import org.scalatest.Suite
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import common.io.MemoryRoot
import common.io.avro.Avroable
import common.test.AuxSpecs

trait AvroableSpecs extends AnyPropSpec with ScalaCheckDrivenPropertyChecks with AuxSpecs {
  self: Suite =>
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 10, workers = 5)
  def propAvroTest[T: Avroable: Arbitrary: Manifest](): Unit = {
    val avro = Avroable[T]
    property(manifest.runtimeClass.getCanonicalName + " to/from record") {
      forAll { t: T =>
        avro.fromRecord(avro.toRecord(t)) shouldReturn t
      }
    }
  }
  def serializationTest[T: Avroable: Arbitrary: Manifest](): Unit =
    property(manifest.runtimeClass.getCanonicalName + " serialization") {
      forAll { ts: Seq[T] =>
        val saver = new AvroableSaver(new MemoryRoot)
        saver.save[T](ts)
        saver.load shouldReturn ts
      }
    }
}
