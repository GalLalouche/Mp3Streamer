package playlist

import backend.search.{FakeModelFactory, FakeModelJsonable}
import common.{AuxSpecs, Jsonable}
import org.scalatest.FreeSpec

class PlaylistQueueTest extends FreeSpec with AuxSpecs with Jsonable.ToJsonableOps {
  private val fakeModelFactory = new FakeModelFactory
  private val fakeJsonable = new FakeModelJsonable
  import fakeJsonable._
  private implicit val jsonable = PlaylistQueue.PlaylistJsonable

  "jsonify and parse" in {
    val $ = PlaylistQueue(Seq(fakeModelFactory.song(), fakeModelFactory.song()))
    $.jsonify.parse shouldReturn $
  }
}
