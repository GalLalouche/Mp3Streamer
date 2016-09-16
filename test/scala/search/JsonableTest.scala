package search

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.Jsonable._

class JsonableTest extends FreeSpec with AuxSpecs {
  def test[T: Jsonable](t: T) {
    implicitly[Jsonable[T]].parse(implicitly[Jsonable[T]].jsonify(t)) shouldReturn t
  }
  "Song" - {
    "Song without optionals" in { test(Models.mockSong(discNumber = None, trackGain = None)) }
    "Song with optionals" in { test(Models.mockSong(discNumber = Some("discno"), trackGain = Some(1.25))) }
  }
  "Album" in { test(Models.mockAlbum()) }
  "Artist" in { test(Models.mockArtist()) }
}
