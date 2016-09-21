package playlist

import common.{AuxSpecs, Debug}
import org.scalatest.FreeSpec
import search.Models

class PlaylistTest extends FreeSpec with AuxSpecs with Debug {
  "jsonify and parse" in {
    val $ = Playlist(Seq(Models.mockSong(), Models.mockSong()))
    val jsonable = Playlist.PlaylistJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
