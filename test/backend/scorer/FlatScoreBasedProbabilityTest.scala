package backend.scorer

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import org.scalatest.mockito.MockitoSugar
import org.scalatest.tags.Slow
import org.scalatest.WordSpec

import backend.logging.Logger
import backend.recon.{Album, Artist}
import common.io.FileRef
import common.rich.collections.RichTraversableOnce._
import common.rich.RichEnumeratum.richEnumeratum
import common.rich.RichRandom.richRandom
import common.test.AuxSpecs
import models.{FakeModelFactory, Song}

@Slow
class FlatScoreBasedProbabilityTest extends WordSpec with AuxSpecs with MockitoSugar {
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
        def randomScore = ModelScore.random(random)
        val modelFactory = new FakeModelFactory
        val songs = Vector.fill(20000)(modelFactory.song())
        val songScores = songs.view.map(_.file: FileRef).map(_ -> randomScore).toMap
        object FakeModelScorer extends CachedModelScorer {
          override def apply(s: Song) = apply(s.file)
          override def apply(a: Artist) = ???
          override def apply(a: Album) = ???
          override def apply(f: FileRef) = songScores.get(f)
          override def fullInfo(s: Song) = ???
        }
        val allFiles = songScores.keys.toVector
        val $ = FlatScoreBasedProbability.withoutAsserts(
          requiredProbability,
          defaultScore = 0.1,
          FakeModelScorer,
          allFiles,
          Logger.Empty,
        )
        val buffer = new ArrayBuffer[FileRef]()
        while (buffer.length < 1000) {
          val nextSong: Song = random.select(songs)
          if ($(nextSong).roll(random))
            buffer += nextSong.file
        }
        // FIXME Also test for defaults
        buffer.percentageSatisfying(songScores(_) == score) shouldBe requiredProbability(
          score,
        ) +- 0.05
      },
    )
  }
}
