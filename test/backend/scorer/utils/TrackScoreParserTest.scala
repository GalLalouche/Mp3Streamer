package backend.scorer.utils

import backend.recon.Reconcilable.SongExtractor
import backend.scorer.{ModelScore, OptionalModelScore}
import models.FakeModelFactory
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class TrackScoreParserTest extends FreeSpec with AuxSpecs {
  // FIXME Also test for empty
  private val factory = new FakeModelFactory()
  private val song = factory.song(
    title = "And then There was Silence",
    albumName = "A Night at the Opera",
    year = 2002,
    artistName = "Blind Guardian",
  )
  private val score = OptionalModelScore.Scored(ModelScore.Amazing)
  "Parse track" in {
    TrackScoreParser(
      s"SONG ; Blind Guardian ;;;  A Night at the Opera (2002) ;;; 10. And then There was Silence === A",
    ).get shouldReturn (song.track, score)
  }
  // "Bijective" in {
  //  AlbumScoreParser(
  //    Function.tupled(OrgScoreFormatter.album _)(albumAndScore),
  //  ).get shouldReturn albumAndScore
  // }
}
