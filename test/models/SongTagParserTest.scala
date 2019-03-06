package models

import common.AuxSpecs
import org.scalatest.FreeSpec

class SongTagParserTest extends FreeSpec with AuxSpecs {
  private def getSong(location: String) = getResourceFile(location)

  "Song" - {
    "parse id3tag" - {
      "mp3" - {
        "common" in {
          val $ = SongTagParser(getSong("song.mp3"))
          $.title shouldReturn "Hidden Track"
          $.artistName shouldReturn "Sentenced"
          $.albumName shouldReturn "Crimson"
          $.track shouldReturn 12
          $.year shouldReturn 2000
          $.bitRate shouldReturn "192"
          $.duration shouldReturn 3
          $.size shouldReturn 75522L
          $.discNumber shouldReturn None
          $.trackGain shouldReturn None
        }
      }
      "parse year correctly" in {
        SongTagParser(getSong("songWithYear.mp3")).year shouldReturn 1999
      }
      "non-empty optionals" in {
        val $: Song = SongTagParser(getSong("songWithMoreInfo.mp3"))
        $.discNumber.get shouldReturn "Foobar"
        $.trackGain.get shouldReturn -1.25
        $.opus.get shouldReturn "Op. 42"
        $.composer.get shouldReturn "Traditional"
        $.conductor.get shouldReturn "yes"
        $.orchestra.get shouldReturn "no"
        $.performanceYear.get shouldReturn 1995
      }
    }

    "flac" - {
      "regular" in {
        val $ = SongTagParser(getSong("flacSong.flac"))
        $.title shouldReturn "Hidden Track"
        $.artistName shouldReturn "Ben Folds Five"
        $.albumName shouldReturn "Whatever and Ever Amen"
        $.track shouldReturn 1 // Track is actually equal to "01/08".
        $.year shouldReturn 1997
        $.discNumber shouldReturn None
        $.trackGain shouldReturn None
      }
      "with optionals" in {
        val $ = SongTagParser(getSong("flacWithMoreInfo.flac"))
        $.discNumber.get shouldReturn "1/2"
        $.trackGain.get shouldReturn 1.25
        $.opus.get shouldReturn "BWV 16"
        $.composer.get shouldReturn "Ben Folds"
        $.conductor.get shouldReturn "Condi"
        $.orchestra.get shouldReturn "Orci"
        $.performanceYear.get shouldReturn 1999
      }
    }
  }
}
