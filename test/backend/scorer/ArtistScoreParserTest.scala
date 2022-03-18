package backend.scorer

import backend.recon.Artist
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class ArtistScoreParserTest extends FreeSpec with AuxSpecs {
  private val artistAndScore = Artist("Blind Guardian") -> ModelScore.Great
  "Parse artist" in {
    ArtistScoreParser("*** ARTIST ; Blind Guardian === Gr").get shouldReturn artistAndScore
  }
  "Bijective" in {
    ArtistScoreParser(Function.tupled(OrgScoreFormatter.artist _)(artistAndScore))
        .get shouldReturn artistAndScore
  }
}
