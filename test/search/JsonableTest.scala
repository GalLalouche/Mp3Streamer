package search

import java.io.File

import common.AuxSpecs
import models.{Artist, Album, Song}
import org.scalatest.FreeSpec
import search.Jsonable._

class JsonableTest extends FreeSpec with AuxSpecs {
  def test[T: Jsonable](t: T) {
    implicitly[Jsonable[T]].parse(implicitly[Jsonable[T]].jsonify(t)) shouldReturn t
  }
  "Song" in {
    test(new Song(file = new File("foobar"), title = "title", artistName = "artistName", albumName = "albumName",
      track = 1, year = 1984, bitrate = "bitrate", duration = 10, size = 20))
  }
  "Album" in {
    test(new Album(dir = new File("foobar").getAbsoluteFile, title = "title", artistName = "artistName", year = 1984))
  }
  "Artist" in {
    val album1 =
      new Album(dir = new File("foobar").getAbsoluteFile, title = "title", artistName = "artistName", year = 1984)
    val album2 =
      new Album(dir = new File("foobar2").getAbsoluteFile, title = "title2", artistName = "artistName2", year = 1985)
    test(new Artist("artistName", Set(album1, album2)))
  }

}
