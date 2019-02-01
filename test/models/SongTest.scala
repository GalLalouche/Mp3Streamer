

package models

import common.AuxSpecs
import org.scalatest.FreeSpec

class SongTest extends FreeSpec with AuxSpecs {
  private def getSong(location: String) = getResourceFile(location)

  "Song" - {
    "parse id3tag" - {
      "mp3" - {
        "commmon" in {
          val $ = Song(getSong("song.mp3"))
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
        Song(getSong("songWithYear.mp3")).year shouldReturn 1999
      }
      "non-empty optionals" in {
        val $: Song = Song(getSong("songWithMoreInfo.mp3"))
        $.discNumber.get shouldReturn "Foobar"
        $.trackGain.get shouldReturn -1.25
        $.opus.get shouldReturn 42
        $.composer.get shouldReturn "Traditional"
        $.performanceYear.get shouldReturn 1995
      }
    }

    "flac" - {
      "regular" in {
        val $ = Song(getSong("flacSong.flac"))
        $.title shouldReturn "Hidden Track"
        $.artistName shouldReturn "Ben Folds Five"
        $.albumName shouldReturn "Whatever and Ever Amen"
        $.track shouldReturn 1
        $.year shouldReturn 1997
        $.discNumber shouldReturn None
        $.trackGain shouldReturn None
      }
      "with optionals" in {
        val $ = Song(getSong("flacWithMoreInfo.flac"))
        $.discNumber.get shouldReturn "1/2"
        $.trackGain.get shouldReturn 1.25
        $.opus.get shouldReturn 16
        $.composer.get shouldReturn "Ben Folds"
        $.performanceYear.get shouldReturn 1999
      }
    }
  }
}
