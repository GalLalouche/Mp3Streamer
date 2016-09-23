package playlist

import common.{AuxSpecs, Debug}
import org.scalatest.FreeSpec
import search.Models

import scala.concurrent.duration.DurationInt

class PlaylistStateTest extends FreeSpec with AuxSpecs with Debug {
  "jsonify and parse" in {
    val $ = PlaylistState(Seq(Models.mockSong(), Models.mockSong()), 0, 100.seconds)
    val jsonable = PlaylistState.PlaylistStateJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
