package songs.selector.filter.sublinear

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import songs.selector.filter.sublinear.SublinearScalingPercentageTest.{ArbParameters, Parameters}

import cats.implicits.catsSyntaxApplyOps

import common.Percentage
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.test.{Arbitraries, AuxSpecs}
import common.test.kats.GenInstances.MonadGen

class SublinearScalingPercentageTest
    extends AnyPropSpec
    with AuxSpecs
    with ScalaCheckDrivenPropertyChecks {
  property("f(1) = 1") {
    forAll { params: Parameters =>
      params(1) shouldReturn 1
    }
  }
  property("Is monotonous") {
    forAll { params: Parameters =>
      for ((a, b) <- (1 to 100).pairSliding)
        params(a) shouldBe >=(params(b))
    }
  }
  property("Converges to exponent") {
    val min = 1000
    val max = min * 2
    forAll(ArbParameters.arbitrary, Gen.choose(min, max)) { (params, quantity) =>
      whenever(quantity >= min) {
        (params(quantity).p - (params.undampened(quantity).p)) shouldBe <=(0.01)
      }
    }
  }
  property("Dampens") {
    forAll(ArbParameters.arbitrary, Gen.choose(2, 100)) { (params, quantity) =>
      whenever(params.dampingFactor > 0) {
        params(quantity) shouldBe >=(params.undampened(quantity))
      }
    }
  }
}

object SublinearScalingPercentageTest {
  private case class Parameters(
      scalingFactor: Percentage,
      dampingFactor: Double,
  ) {
    def apply(quantity: Int): Percentage =
      SublinearScalingPercentage.formula(quantity, scalingFactor.inverse.p, dampingFactor)
    def undampened(quantity: Int): Percentage =
      SublinearScalingPercentage.formula(quantity, scalingFactor.inverse.p, 0)
  }

  private implicit val ArbParameters: Arbitrary[Parameters] =
    Arbitrary(Arbitraries.genPercentage.map2(Gen.choose(0.0, 10.0))(Parameters))
}
