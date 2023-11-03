package backend.scorer.utils

import org.scalatest.FreeSpec

import backend.recon.Artist
import backend.scorer.ModelScore
import common.test.AuxSpecs

class ArtistScoreParserTest extends FreeSpec with AuxSpecs {
  // FIXME Also test for empty
  private val artistAndScore = Artist("Blind Guardian") -> Some(ModelScore.Great)
  "Parse artist" in {
    ArtistScoreParser("*** ARTIST ; Blind Guardian === Gr").get shouldReturn artistAndScore
  }
  "Bijective" in {
    ArtistScoreParser(
      Function.tupled(OrgScoreFormatter.artist _)(artistAndScore),
    ).get shouldReturn artistAndScore
  }
}
