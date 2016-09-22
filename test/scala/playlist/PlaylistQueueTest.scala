package playlist

import common.{AuxSpecs, Debug}
import org.scalatest.FreeSpec
import search.Models

class PlaylistQueueTest extends FreeSpec with AuxSpecs with Debug {
  "jsonify and parse" in {
    val $ = PlaylistQueue(Seq(Models.mockSong(), Models.mockSong()))
    val jsonable = PlaylistQueue.PlaylistJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
