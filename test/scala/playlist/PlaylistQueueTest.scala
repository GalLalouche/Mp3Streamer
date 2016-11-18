package playlist

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.Models

class PlaylistQueueTest extends FreeSpec with AuxSpecs {
  "jsonify and parse" in {
    val $ = PlaylistQueue(Seq(Models.mockSong(), Models.mockSong()))
    val jsonable = PlaylistQueue.PlaylistJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
