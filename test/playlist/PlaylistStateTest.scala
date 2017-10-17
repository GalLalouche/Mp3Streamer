package playlist

import common.JsonableSpecs
import models.{FakeModelFactory, FakeModelJsonable}

import scala.concurrent.duration.DurationInt

class PlaylistStateTest extends JsonableSpecs {
  private val fakeModelFactory = new FakeModelFactory
  private val fakeModelJsonable = new FakeModelJsonable
  import fakeModelJsonable._

  "jsonify and parse" in jsonTest(PlaylistState(Seq(fakeModelFactory.song(), fakeModelFactory.song()), 0, 100.seconds))

}
