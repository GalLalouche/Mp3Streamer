package songs.selector

import backend.module.TestModuleConfiguration
import models.{FakeModelFactory, MemorySong}
import musicfinder.FakeMusicFiles
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalacheck.Arbitrary._
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import common.test.AuxSpecs

class SongSelectorTest
    extends AnyFreeSpec
    with OneInstancePerTest
    with AuxSpecs
    with ScalaCheckDrivenPropertyChecks {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 10, workers = 5)
  private val factory = new FakeModelFactory()
  import factory.arbSong

  "returns a random song" in {
    forAll { ss: Vector[MemorySong] =>
      whenever(ss.nonEmpty) {
        val injector = TestModuleConfiguration().injector
        val mf = injector.instance[FakeMusicFiles]
        val songs = ss.map(mf.copySong)

        val $ = injector.instance[SongSelector]

        songs should contain($.randomSong())
      }
    }
  }
}
