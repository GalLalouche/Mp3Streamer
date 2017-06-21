package playlist

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.FakeModelFactory

class PlaylistQueueTest extends FreeSpec with AuxSpecs {
  private val fakeModelFactory = new FakeModelFactory
  "jsonify and parse" in {
    val $ = PlaylistQueue(Seq(fakeModelFactory.song(), fakeModelFactory.song()))
    val jsonable = PlaylistQueue.PlaylistJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
