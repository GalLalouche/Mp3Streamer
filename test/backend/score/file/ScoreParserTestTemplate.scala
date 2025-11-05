package backend.score.file

import backend.score.{ModelScore, OptionalModelScore}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxFlatMapOps, toFlatMapOps}
import common.test.kats.GenInstances.MonadGen
import common.test.{AuxSpecs, MoreGen}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

abstract class ScoreParserTestTemplate[A: Arbitrary]
    extends AnyFreeSpec
    with AuxSpecs
    with ScalaCheckDrivenPropertyChecks {
  protected def basicInput: (A, String)
  protected def parse: String => (A, OptionalModelScore)
  protected def format: (A, OptionalModelScore) => String
  private implicit val optionalModelScoreGen: Gen[OptionalModelScore] = Gen.oneOf(
    OptionalModelScore.Default.pure,
    Gen.oneOf(ModelScore.values).map(OptionalModelScore.Scored),
  )
  private def toScoreString(score: OptionalModelScore): Gen[String] = {
    val entryName = score.entryName
    MoreGen.prefix(entryName, if (entryName.head == 'G') 2 else 1) >>= MoreGen.capitalization
  }
  "Parse basic" in {
    forAll(optionalModelScoreGen.mproduct(toScoreString)) { (ss: (OptionalModelScore, String)) =>
      val (a, s) = basicInput
      parse(s + " === " + ss._2) == (a, ss._1)
    }
  }

  "Bijective" in {
    forAll((a: A, score: OptionalModelScore) => parse(format(a, score)) == (a, score))
  }
}
