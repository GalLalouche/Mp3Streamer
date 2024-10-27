package songs.selector

import backend.module.{FakeMusicFinder, TestModuleConfiguration}
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import common.test.AuxSpecs

class FollowingSongTest
    extends FreeSpec
    with OneInstancePerTest
    with AuxSpecs
    with GeneratorDrivenPropertyChecks
    with Matchers {
  private val factory = new FakeModelFactory()
  "next song" in {
    val injector = TestModuleConfiguration().injector
    val mf = injector.instance[FakeMusicFinder]
    val song1 =
      mf.copySong(factory.song(albumName = "album", artistName = "artist", trackNumber = 1))
    val song2 =
      mf.copySong(factory.song(albumName = "album", artistName = "artist", trackNumber = 2))

    val $ = injector.instance[FollowingSong]

    val nextSong = $.next(song1).get
    nextSong shouldReturn song2
    $.next(song2) shouldBe 'empty
  }
}
