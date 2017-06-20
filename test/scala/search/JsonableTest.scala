package search

import common.{AuxSpecs, Jsonable}
import org.scalatest.FreeSpec
import search.ModelsJsonable._

class JsonableTest extends FreeSpec with AuxSpecs with Jsonable.ToJsonableOps {
  def test[T: Jsonable](t: T) {
    parseObject[T](t.jsonify).parse shouldReturn t
  }
  "Song" - {
    "Song without optionals" in {
      test(FakeModelFactory.song(discNumber = None, trackGain = None))
    }
    "Song with optionals" in {
      test(FakeModelFactory.song(discNumber = Some("discno"), trackGain = Some(1.25)))
    }
  }
  "Album" in { test(FakeModelFactory.album()) }
  "Artist" in { test(FakeModelFactory.artist()) }
}
