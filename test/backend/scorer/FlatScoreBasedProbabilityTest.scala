package backend.scorer

import backend.logging.Logger
import backend.recon.{Album, Artist}
import models.{FakeModelFactory, Song}
import org.scalatest.WordSpec
import org.scalatest.mockito.MockitoSugar
import org.scalatest.tags.Slow

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import common.io.FileRef
import common.rich.collections.RichTraversableOnce._
import common.rich.RichEnumeratum.richEnumeratum
import common.rich.RichRandom.richRandom
import common.rich.RichT.richT
import common.test.AuxSpecs

@Slow
class FlatScoreBasedProbabilityTest extends WordSpec with AuxSpecs with MockitoSugar {
  private val requiredProbability: ModelScore => Double = {
    case ModelScore.Crappy => 0.08
    case ModelScore.Meh => 0.05
    case ModelScore.Okay => 0.1
    case ModelScore.Good => 0.15
    case ModelScore.Great => 0.2
    case ModelScore.Amazing => 0.25
    case ModelScore.Classic => 0.17
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
        buffer.percentageSatisfying(songScores(_) == score) shouldBe requiredProbability(score) +- 0.05
      })
  }
}
