package songs

import backend.module.{FakeMusicFinder, TestModuleConfiguration}
import backend.recon.{Album, Artist}
import backend.scorer.{CachedModelScorer, ModelScore, ScoreBasedProbability}
import com.google.inject.util.Modules
import com.google.inject.Guice
import models.{FakeModelFactory, MemorySong, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import net.codingwell.scalaguice.ScalaModule
import org.scalacheck.Arbitrary._
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import common.io.FileRef
import common.test.AuxSpecs

class SongSelectorTest extends FreeSpec with OneInstancePerTest with AuxSpecs with GeneratorDrivenPropertyChecks
    with Matchers {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 10, workers = 5)
  private val factory = new FakeModelFactory()
  import factory.arbSong

  private def createInjector = Guice.createInjector(
    Modules.`override`(TestModuleConfiguration().module).`with`(new ScalaModule {
      override def configure(): Unit = {
        bind[ScoreBasedProbability].toInstance(new ScoreBasedProbability {
          override def apply(s: Song) = 0.5
          override def apply(s: ModelScore) = 0.5
        })
        bind[CachedModelScorer].toInstance(new CachedModelScorer {
          override def apply(a: Artist) = ???
          override def apply(a: Album) = ???
          override def apply(s: Song) = Some(ModelScore.Okay)
          override def apply(f: FileRef) = ???
        })
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
