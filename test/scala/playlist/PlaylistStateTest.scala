package playlist

import backend.search.{FakeModelFactory, FakeModelJsonable}
import common.{AuxSpecs, Jsonable}
import org.scalatest.FreeSpec

import scala.concurrent.duration.DurationInt

class PlaylistStateTest extends FreeSpec with AuxSpecs with Jsonable.ToJsonableOps {
  private val fakeModelFactory = new FakeModelFactory
  private val fakeModelJsonable = new FakeModelJsonable
  import fakeModelJsonable._
  private implicit val jsonable = PlaylistState.PlaylistStateJsonable

  "jsonify and parse" in {
    val $ = PlaylistState(Seq(fakeModelFactory.song(), fakeModelFactory.song()), 0, 100.seconds)
    $.jsonify.parse shouldReturn $
  }
}
