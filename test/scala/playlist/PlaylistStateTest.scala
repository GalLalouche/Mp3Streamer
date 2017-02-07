package playlist

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.Models

import scala.concurrent.duration.DurationInt

class PlaylistStateTest extends FreeSpec with AuxSpecs {
  "jsonify and parse" in {
    val $ = PlaylistState(Seq(Models.mockSong(), Models.mockSong()), 0, 100.seconds)
    val jsonable = PlaylistState.PlaylistStateJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
