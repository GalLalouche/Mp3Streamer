package playlist

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.FakeModelFactory

import scala.search.FakeModelJsonable

class PlaylistQueueTest extends FreeSpec with AuxSpecs {
  private val fakeModelFactory = new FakeModelFactory
  private val fakeJsonable = new FakeModelJsonable
  import fakeJsonable._
  "jsonify and parse" in {
    val $ = PlaylistQueue(Seq(fakeModelFactory.song(), fakeModelFactory.song()))
    val jsonable = PlaylistQueue.PlaylistJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
