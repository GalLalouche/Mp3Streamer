

package models

import common.AuxSpecs
import org.scalatest.FreeSpec

class SongTest extends FreeSpec with AuxSpecs {
  private def getSong(location: String) = getResourceFile(location)
  val song = getSong("song.mp3")
  val $ = Song(song)

  "Song" - {
    "parse id3tag" in {
      $.title shouldReturn "Hidden Track"
      $.artistName shouldReturn "Sentenced"
      $.albumName shouldReturn "Crimson"
      $.track shouldReturn 12
      $.year shouldReturn 2000
      $.bitrate shouldReturn "192"
      $.duration shouldReturn 3
      $.size shouldReturn 75522L
    }
    "parse year correctly" in {
      Song(getSong("songWithYear.mp3")).year shouldReturn 1999
    }
  }
}
