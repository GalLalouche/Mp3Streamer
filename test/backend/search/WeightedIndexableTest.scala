package backend.search

import backend.search.WeightedIndexable.ops._
import models.{FakeModelFactory, Song}
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class WeightedIndexableTest extends FreeSpec with AuxSpecs {
  private val factory = new FakeModelFactory
  private val song = factory
    .song(title = "foo bar", artistName = "quxx", year = 1999, albumName = "bazz bazz")
    .asInstanceOf[Song]
  private val classicalSong = factory
    .song(
      title = "foo, bar",
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
    factory
      .artist(
        name = "foo foo bar",
        albums = Vector(factory.album(title = "should be ignored")),
      )
      .terms shouldMultiSetEqual Vector("foo" -> 1.0, "bar" -> 1.0)
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
