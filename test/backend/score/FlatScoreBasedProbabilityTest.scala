package backend.score

import backend.recon.{Album, Artist, Track}
import backend.recon.Reconcilable.SongExtractor
import models.{FakeModelFactory, Song}
import org.scalatest.WordSpec
import org.scalatest.tags.Slow
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import common.io.FileRef
import common.rich.RichEnumeratum.richEnumeratum
import common.rich.RichRandomSpecVer.richRandomSpecVer
import common.rich.collections.RichTraversableOnce._
import common.test.AuxSpecs

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
        val songs = Vector.tabulate(20000)(i => modelFactory.song(title = i.toString))
        val tracks = songs.mapBy(_.track)
        val songScores = songs.view.map(_.file: FileRef).map(_ -> randomScore).toMap
        object FakeModelScorer extends CachedModelScorer {
          override def explicitScore(a: Artist) = ???
          override def explicitScore(a: Album) = ???
          override def explicitScore(t: Track) = ???
          override def aggregateScore(f: FileRef) = songScores.get(f).toOptionalModelScore
          override def aggregateScore(t: Track) = aggregateScore(tracks(t).file)
          override def fullInfo(t: Track) = ???
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
        // FIXME Also test for defaults
        buffer.percentageSatisfying(songScores(_) == score) shouldBe requiredProbability(
          score,
        ) +- 0.05
      },
    )
  }
}
