package backend.recon

import org.scalacheck.{Arbitrary, Gen}

object ReconIDArbitrary {
  def gen: Gen[ReconID] = Gen
    .listOfN(32, Gen.hexChar.map(_.toLower))
    .map(_.mkString)
    .map(str =>
      ReconID.validateOrThrow(s"${str.substring(0, 8)}-${str.substring(8, 12)}-${str
          .substring(12, 16)}-${str.substring(16, 20)}-${str.substring(20, 32)}"),
    )

  implicit def ArbitraryReconId: Arbitrary[ReconID] = Arbitrary(gen)
}
