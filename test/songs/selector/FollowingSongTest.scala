package songs.selector

import backend.module.TestModuleConfiguration
import models.FakeModelFactory
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import common.test.AuxSpecs

class FollowingSongTest
    extends AnyFreeSpec
    with OneInstancePerTest
    with AuxSpecs
    with ScalaCheckDrivenPropertyChecks {
  private val factory = new FakeModelFactory()
  "next song" in {
    val injector = TestModuleConfiguration().injector
    val mf = injector.instance[FakeMusicFiles]
    val song1 =
      mf.copySong(factory.song(albumName = "album", artistName = "artist", trackNumber = 1))
    val song2 =
      mf.copySong(factory.song(albumName = "album", artistName = "artist", trackNumber = 2))

    val $ = injector.instance[FollowingSong]

    val nextSong = $.next(song1).get
    nextSong shouldReturn song2
    $.next(song2) shouldBe empty
  }
}
