package playlist

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.FakeModelFactory

import scala.search.FakeModelsJsonable

class PlaylistQueueTest extends FreeSpec with AuxSpecs {
  private val fakeModelFactory = new FakeModelFactory
  private val fakeJsonable = new FakeModelsJsonable
  import fakeJsonable._
  "jsonify and parse" in {
    val $ = PlaylistQueue(Seq(fakeModelFactory.song(), fakeModelFactory.song()))
    val jsonable = PlaylistQueue.PlaylistJsonable
    jsonable.parse(jsonable.jsonify($)) shouldReturn $
  }
}
