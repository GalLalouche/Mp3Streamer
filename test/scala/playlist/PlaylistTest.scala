package playlist

import backend.configs.TestConfiguration
import common.{AuxSpecs, Debug}
import org.scalatest.FreeSpec
import search.Models

class PlaylistTest extends FreeSpec with AuxSpecs with Debug {
  private implicit val c = TestConfiguration()
  import c._
  "save and load" in {
    val $ = Playlist(Seq(Models.mockSong(), Models.mockSong()))
    Playlist.save($)
    Playlist.load shouldReturn $
  }
}
