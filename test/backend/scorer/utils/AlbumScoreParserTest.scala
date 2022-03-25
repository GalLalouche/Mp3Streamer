package backend.scorer.utils

import backend.recon.{Album, Artist}
import backend.scorer.ModelScore
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class AlbumScoreParserTest extends FreeSpec with AuxSpecs {
  private val albumAndScore =
    Album("A Night at the Opera", 2002, Artist("Blind Guardian")) -> ModelScore.Amazing
  "Parse album" in {
    AlbumScoreParser("** ALBUM ; Blind Guardian ;;;  A Night at the Opera (2002) === A")
        .get shouldReturn albumAndScore
  }
  "Bijective" in {
    AlbumScoreParser(Function.tupled(OrgScoreFormatter.album _)(albumAndScore)).get shouldReturn albumAndScore
  }
}
