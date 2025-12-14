package backend.search

import backend.search.WeightedIndexable.ops._
import models.{FakeModelFactory, Song}
import org.scalatest.freespec.AnyFreeSpec

import common.test.AuxSpecs

class WeightedIndexableTest extends AnyFreeSpec with AuxSpecs {
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
      .terms shouldMultiSetEqual Vector("foo" -> 10, "bar" -> 10)
  }
  "song" - {
    "regular song" in {
      song.terms shouldMultiSetEqual
        Vector("foo" -> 10, "bar" -> 10, "bazz" -> 1, "quxx" -> 1, "1999" -> 1)
    }
    "classical song" in {
      classicalSong.terms shouldMultiSetEqual Vector(
        "foo" -> 10,
        "bar" -> 10,
        "bazz" -> 1,
        "quxx" -> 1,
        "1999" -> 1,
        "compy" -> 1,
        "orchy" -> 1,
        "opy" -> 1,
        "2001" -> 1,
      )
    }
  }
  "Album" in {
    factory.album(title = "foo1 bar1 bar1", songs = Vector(song)).terms shouldMultiSetEqual
      Vector("foo1" -> 10, "bar1" -> 10, "bazz" -> 1, "quxx" -> 1, "1999" -> 1)
  }
}
