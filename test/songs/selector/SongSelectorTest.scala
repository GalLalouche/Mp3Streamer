package songs.selector

import backend.module.{FakeMusicFinder, TestModuleConfiguration}
import backend.recon.{Album, Artist, Track}
import backend.score.{IndividualScorer, ModelScore, OptionalModelScore, ScoreBasedProbability}
import com.google.inject.Guice
import genre.GenreFinder
import models.{FakeModelFactory, MemorySong, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import net.codingwell.scalaguice.ScalaModule
import org.scalacheck.Arbitrary._
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import common.guice.RichModule.richModule
import common.io.DirectoryRef
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

  private def createInjector = Guice.createInjector(
    TestModuleConfiguration().module.overrideWith(new ScalaModule {
      override def configure(): Unit = {
        bind[GenreFinder].toInstance(new GenreFinder(null) {
          override def forArtist(artist: Artist) = None
          override def apply(dir: DirectoryRef) = ???
        })
        bind[ScoreBasedProbability].toInstance(new ScoreBasedProbability {
          override def apply(s: Song) = 0.5
          override def apply(s: ModelScore) = 0.5
        })
        bind[IndividualScorer].toInstance(new IndividualScorer {
          override def explicitScore(a: Artist): OptionalModelScore = ???
          override def explicitScore(a: Album): OptionalModelScore = ???
          override def explicitScore(t: Track) = OptionalModelScore.Scored(ModelScore.Okay)
        })
      }
    }),
  )
  "returns a random song" in {
    forAll { ss: Vector[MemorySong] =>
      whenever(ss.nonEmpty) {
        val injector = createInjector
        val mf = injector.instance[FakeMusicFinder]
        val songs = ss.map(mf.copySong)

        val $ = injector.instance[SongSelector]

        songs should contain($.randomSong())
      }
    }
  }
}
