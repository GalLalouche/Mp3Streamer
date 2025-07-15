package backend.recon

import backend.recon.ReconIDArbitrary.ArbitraryReconId
import org.scalacheck.Prop.forAll
import org.scalatest.PropSpec

class ReconIDArbitraryTest extends PropSpec {
  property("Recon ID is valid") {
    forAll { reconId: ReconID =>
      // This is actually already covered by the generator calling validate, so this test is being
      // extra paranoid.
      ReconID.validate(reconId.id).isDefined
    }
  }
}
