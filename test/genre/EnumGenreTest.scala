package genre

import common.test.AuxSpecs
import genre.Genre._
import org.scalatest.freespec.AnyFreeSpec

class EnumGenreTest extends AnyFreeSpec with AuxSpecs {
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
