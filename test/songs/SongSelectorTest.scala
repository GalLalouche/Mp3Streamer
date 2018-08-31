package songs

import backend.configs.{FakeMusicFinder, TestConfiguration}
import common.AuxSpecs
import models.{FakeModelFactory, MemorySong}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalacheck.Arbitrary._
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class SongSelectorTest extends FreeSpec with OneInstancePerTest with AuxSpecs with GeneratorDrivenPropertyChecks
    with Matchers {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 10, workers = 5)
  private val factory = new FakeModelFactory()
  import factory.arbSong

  "returns a random song" in {
    forAll {ss: List[MemorySong] =>
      whenever(ss.nonEmpty) {
        implicit val c: TestConfiguration = new TestConfiguration
        val mf = c.injector.instance[FakeMusicFinder]
        val songs = ss.map(mf.copySong)

        val $ = c.injector.instance[SongSelectorFactory].create()

        songs should contain($.randomSong)
      }
    }
  }
  "next song" in {
    implicit val c: TestConfiguration = new TestConfiguration
    val mf = c.injector.instance[FakeMusicFinder]
    val song1 = mf.copySong(factory.song(albumName = "album", artistName = "artist", track = 1))
    val song2 = mf.copySong(factory.song(albumName = "album", artistName = "artist", track = 2))

    val $ = c.injector.instance[SongSelectorFactory].create()

    val nextSong = $.followingSong(song1).get
    nextSong shouldReturn song2
    $ followingSong song2 shouldBe 'empty
  }
}
