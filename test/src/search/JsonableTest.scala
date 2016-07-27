package search

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers
import search.Jsonable._

class JsonableTest extends FreeSpec with ShouldMatchers {
  def test[T: Jsonable](t: T) {
    implicitly[Jsonable[T]].parse(implicitly[Jsonable[T]].jsonify(t)) should be === t
  }
  "Song" in { test(Models.mockSong()) }
  "Album" in { test(Models.mockAlbum()) }
  "Artist" in { test(Models.mockArtist()) }
}
