package playlist

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.FakeModelFactory

import scala.concurrent.duration.DurationInt

class PlaylistStateTest extends FreeSpec with AuxSpecs {
  private val fakeModelFactory = new FakeModelFactory
  "jsonify and parse" in {
    val $ = PlaylistState(Seq(fakeModelFactory.song(), fakeModelFactory.song()), 0, 100.seconds)
    val jsonable = PlaylistState.PlaylistStateJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
