package songs

import backend.configs.{FakeMusicFinder, TestConfiguration}
import common.AuxSpecs
import models.MemorySong
import org.scalacheck.Arbitrary._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FreeSpec, OneInstancePerTest, ShouldMatchers}
import search.FakeModelFactory

class SongSelectorTest extends FreeSpec with OneInstancePerTest with AuxSpecs with GeneratorDrivenPropertyChecks
    with ShouldMatchers {
  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 10, workers = 5)
  private val factory = new FakeModelFactory()
  import factory.arbSong

  "returns a random song" in {
    forAll {ss: List[MemorySong] =>
      whenever(ss.nonEmpty) {
        implicit val c = new TestConfiguration
        val mf: FakeMusicFinder = c.mf
        val songs = ss.map(mf.copySong)

        val $ = SongSelector.create

        songs should contain($.randomSong)
      }
    }
  }
  "next song" in {
    implicit val c = new TestConfiguration
    val mf: FakeMusicFinder = c.mf
    val song1 = mf.copySong(factory.song(albumName = "album", artistName = "artist", track = 1))
    val song2 = mf.copySong(factory.song(albumName = "album", artistName = "artist", track = 2))

    val $ = SongSelector.create

    val nextSong = $.followingSong(song1).get
    nextSong shouldReturn song2
    $ followingSong song2 shouldBe 'empty
  }
}
