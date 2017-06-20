package playlist

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.FakeModelFactory

import scala.concurrent.duration.DurationInt

class PlaylistStateTest extends FreeSpec with AuxSpecs {
  "jsonify and parse" in {
    val $ = PlaylistState(Seq(FakeModelFactory.mockSong(), FakeModelFactory.mockSong()), 0, 100.seconds)
    val jsonable = PlaylistState.PlaylistStateJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
