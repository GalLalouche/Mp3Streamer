package playlist

import common.JsonableSpecs
import models.{FakeModelFactory, FakeModelJsonable}

class PlaylistQueueTest extends JsonableSpecs {
  private val fakeModelFactory = new FakeModelFactory
  private val fakeJsonable = new FakeModelJsonable
  import fakeJsonable._

  "jsonify and parse" in jsonTest(PlaylistQueue(Seq(fakeModelFactory.song(), fakeModelFactory.song())))
}
