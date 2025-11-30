package backend.score

import backend.recon.Reconcilable.SongExtractor
import backend.recon.Track
import models.{FakeModelFactory, Song}
import org.scalatest.tags.Slow
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import common.io.FileRef
import common.rich.RichEnumeratum.richEnumeratum
import common.rich.RichRandom.richRandom
import common.rich.RichRandomSpecVer.richRandomSpecVer
import common.rich.RichT.lazyT
import common.rich.collections.RichTraversableOnce._
import common.test.AuxSpecs

@Slow
class FlatScoreBasedProbabilityTest extends AnyWordSpec with AuxSpecs with MockitoSugar {
  private val requiredProbability: ModelScore => Double = {
    case ModelScore.Crappy => 0.11
    case ModelScore.Meh => 0.08
    case ModelScore.Okay => 0.13
    case ModelScore.Good => 0.18
    case ModelScore.Great => 0.22
    case ModelScore.Amazing => 0.28
  }

  "apply" should {
    ModelScore.values.foreach(score =>
      s"return correct probability for $score" in {
        val random = new Random
        def randomScore: ModelScore = ModelScore.random(random)
        val modelFactory = new FakeModelFactory
        val songs = Vector.tabulate(20000)(i => modelFactory.song(title = i.toString))
        val tracks = songs.mapBy(_.track)
        val songScores = songs.view.map(_.file: FileRef).map(_ -> randomScore).toMap
        object FakeModelScorer extends AggregateScorer {
          override def aggregateScore(f: FileRef) =
            songScores(f).onlyIf(random.flipCoin(0.95)).toOptionalModelScore
          override def aggregateScore(t: Track) = aggregateScore(tracks(t).file)
        }
        val allFiles = songScores.keys.toVector
        val $ = FlatScoreBasedProbability.withoutAsserts(
          requiredProbability,
          defaultScore = 0.1,
          FakeModelScorer,
          allFiles,
        )
        val buffer = new ArrayBuffer[FileRef]()
        while (buffer.length < 1000) {
          val nextSong: Song = random.select(songs)
          if ($(nextSong).roll(random))
            buffer += nextSong.file
        }
        buffer.percentageSatisfying(songScores(_) == score) shouldBe requiredProbability(
          score,
        ) +- 0.05
      },
    )
  }
}
