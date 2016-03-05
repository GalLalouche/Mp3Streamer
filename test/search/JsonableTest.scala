package search

import common.AuxSpecs
import org.scalatest.FreeSpec
import search.Jsonable._

class JsonableTest extends FreeSpec with AuxSpecs {
  def test[T: Jsonable](t: T) {
    implicitly[Jsonable[T]].parse(implicitly[Jsonable[T]].jsonify(t)) shouldReturn t
  }
  "Song" in { test(Models.mockSong()) }
  "Album" in { test(Models.mockAlbum()) }
  "Artist" in { test(Models.mockArtist()) }
}
