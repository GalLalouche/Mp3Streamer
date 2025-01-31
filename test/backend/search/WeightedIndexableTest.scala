package backend.search

import backend.search.WeightedIndexable.ops._
import models.{ArtistDir, FakeModelFactory, Song}
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class WeightedIndexableTest extends FreeSpec with AuxSpecs {
  val factory = new FakeModelFactory
  val song = factory
    .song(title = "foo bar", artistName = "quxx", year = 1999, albumName = "bazz bazz")
    .asInstanceOf[Song]
  val classicalSong = factory
    .song(
      title = "foo bar",
      artistName = "bar quxx",
      year = 1999,
      albumName = "bazz bazz",
      composer = Some("compy"),
      orchestra = Some("orchy"),
      opus = Some("opy"),
      performanceYear = Some(2001),
    )
    .asInstanceOf[Song]
  "Artist" in {
    ArtistDir(
      "foo foo bar",
      Set(factory.album(title = "should be ignored")),
    ).terms shouldMultiSetEqual
      Vector("foo" -> 1.0, "bar" -> 1.0)
  }
  "song" - {
    "regular song" in {
      song.terms shouldMultiSetEqual
        Vector("foo" -> 1.0, "bar" -> 1.0, "bazz" -> 0.1, "quxx" -> 0.1, "1999" -> 0.1)
    }
    "classical song" in {
      classicalSong.terms shouldMultiSetEqual Vector(
        "foo" -> 1.0,
        "bar" -> 1.0,
        "bazz" -> 0.1,
        "quxx" -> 0.1,
        "1999" -> 0.1,
        "compy" -> 0.1,
        "orchy" -> 0.1,
        "opy" -> 0.1,
        "2001" -> 0.1,
      )
    }
  }
  "Album" in {
    factory.album(title = "foo1 bar1 bar1", songs = Vector(song)).terms shouldMultiSetEqual
      Vector("foo1" -> 1.0, "bar1" -> 1.0, "bazz" -> 0.1, "quxx" -> 0.1, "1999" -> 0.1)
  }
}
