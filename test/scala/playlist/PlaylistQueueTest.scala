package playlist

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.FakeModelFactory

class PlaylistQueueTest extends FreeSpec with AuxSpecs {
  "jsonify and parse" in {
    val $ = PlaylistQueue(Seq(FakeModelFactory.mockSong(), FakeModelFactory.mockSong()))
    val jsonable = PlaylistQueue.PlaylistJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
