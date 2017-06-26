package search

import common.{AuxSpecs, Jsonable}
import org.scalatest.FreeSpec
import search.ModelsJsonable._

class JsonableTest extends FreeSpec with AuxSpecs with Jsonable.ToJsonableOps {
  private val fakeModelFactory = new FakeModelFactory
  def test[T: Jsonable](t: T) {
    val actual = parseObject[T](t.jsonify).parse
    val expected = t
    actual shouldReturn expected
  }
  "Song" - {
    "Song without optionals" in {
      test(fakeModelFactory.song(discNumber = None, trackGain = None))
    }
    "Song with optionals" in {
      test(fakeModelFactory.song(discNumber = Some("discno"), trackGain = Some(1.25)))
    }
  }
  "Album" in { test(fakeModelFactory.album()) }
  "Artist" in { test(fakeModelFactory.artist()) }
}
