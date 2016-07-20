

package models

import java.io.File

import org.scalatest.FreeSpec
import play.api.Play
import play.api.test.FakeApplication

class SongTest extends FreeSpec {
  private def getSong(location: String) =
    new File(Play.application(FakeApplication()).resource(location).get.getFile.replaceAll("%20", " "))
  val song = getSong("./resources/songs/song.mp3")
  val $ = Song(song)

  "Song" - {
    "parse id3tag" - {
      $.title === "Hidden Track"
      $.artistName === "Sentenced"
      $.albumName === "Crimson"
      $.track === 12
      $.year === 2000
      $.bitrate === "192"
      $.duration === 3
      $.size === 75522L
    }
    "parse year correctly" - {
      Song(getSong("./resources/songs/songWithYear.mp3")).year === 1999
    }
  }
}
