package songs

import backend.module.{FakeMusicFinder, TestModuleConfiguration}
import models.{FakeModelFactory, MemorySong}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalacheck.Arbitrary._
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import common.test.AuxSpecs

class SongSelectorTest extends FreeSpec with OneInstancePerTest with AuxSpecs with GeneratorDrivenPropertyChecks
    with Matchers {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 10, workers = 5)
  private val factory = new FakeModelFactory()
  import factory.arbSong

  "returns a random song" in {
    forAll {ss: Vector[MemorySong] =>
      whenever(ss.nonEmpty) {
        val c = new TestModuleConfiguration
        val mf = c.injector.instance[FakeMusicFinder]
        val songs = ss.map(mf.copySong)

        val $ = c.injector.instance[SongSelector]

        songs should contain($.randomSong)
      }
    }
  }
  "next song" in {
    val c = new TestModuleConfiguration
    val mf = c.injector.instance[FakeMusicFinder]
    val song1 = mf.copySong(factory.song(albumName = "album", artistName = "artist", track = 1))
    val song2 = mf.copySong(factory.song(albumName = "album", artistName = "artist", track = 2))

    val $ = c.injector.instance[SongSelector]

    val nextSong = $.followingSong(song1).get
    nextSong shouldReturn song2
    $ followingSong song2 shouldBe 'empty
  }
}
