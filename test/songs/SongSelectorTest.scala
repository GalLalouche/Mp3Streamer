package songs

import backend.module.{FakeMusicFinder, TestModuleConfiguration}
import backend.scorer.ScoreBasedProbability
import com.google.inject.util.Modules
import com.google.inject.Guice
import models.{FakeModelFactory, MemorySong}
import net.codingwell.scalaguice.InjectorExtensions._
import net.codingwell.scalaguice.ScalaModule
import org.scalacheck.Arbitrary._
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import common.test.AuxSpecs
import common.Percentage

class SongSelectorTest extends FreeSpec with OneInstancePerTest with AuxSpecs with GeneratorDrivenPropertyChecks
    with Matchers {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 10, workers = 5)
  private val factory = new FakeModelFactory()
  import factory.arbSong

  private def createInjector = Guice.createInjector(
    Modules.`override`(TestModuleConfiguration().module).`with`(new ScalaModule {
      override def configure(): Unit = {
        bind[ScoreBasedProbability].toInstance(_ => Percentage(0.5))
      }
    })
  )
  "returns a random song" in {
    forAll {ss: Vector[MemorySong] =>
      whenever(ss.nonEmpty) {
        val injector = createInjector
        val mf = injector.instance[FakeMusicFinder]
        val songs = ss.map(mf.copySong)

        val $ = injector.instance[SongSelector]

        songs should contain($.randomSong())
      }
    }
  }
  "next song" in {
    val injector = createInjector
    val mf = injector.instance[FakeMusicFinder]
    val song1 = mf.copySong(factory.song(albumName = "album", artistName = "artist", track = 1))
    val song2 = mf.copySong(factory.song(albumName = "album", artistName = "artist", track = 2))

    val $ = injector.instance[SongSelector]

    val nextSong = $.followingSong(song1).get
    nextSong shouldReturn song2
    $ followingSong song2 shouldBe 'empty
  }
}
