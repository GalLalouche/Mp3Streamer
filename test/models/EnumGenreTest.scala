package models

import models.Genre._
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class EnumGenreTest extends FreeSpec with AuxSpecs {
  "ordering" - {
    // Helping type inference
    def test(smallGenre: Genre, bigGenre: Genre): Unit =
      s"$smallGenre vs $bigGenre" in {
        smallGenre should be < bigGenre
        bigGenre should be > smallGenre
      }
    "flat vs non-flat" - {
      test(Jazz, NewAge)
      test(Rock("Steady"), NewAge)
      test(Rock("Steady"), Metal("Pedal"))
      test(Metal("Pedal"), Musicals)
      test(Rock("N"), Rock("Roll"))
      test(Metal("ic"), Metal("lica"))
    }
  }
}
