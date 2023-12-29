package backend.scorer.utils

import backend.recon.{Album, Artist}
import backend.scorer.ModelScore
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class AlbumScoreParserTest extends FreeSpec with AuxSpecs {
  // FIXME Also test for empty
  private val albumAndScore =
    Album("A Night at the Opera", 2002, Artist("Blind Guardian")) -> Option(ModelScore.Amazing)
  "Parse album" in {
    AlbumScoreParser(
      "** ALBUM ; Blind Guardian ;;;  A Night at the Opera (2002) === A",
    ).get shouldReturn albumAndScore
  }
  "Bijective" in {
    AlbumScoreParser(
      Function.tupled(OrgScoreFormatter.album _)(albumAndScore),
    ).get shouldReturn albumAndScore
  }
}
