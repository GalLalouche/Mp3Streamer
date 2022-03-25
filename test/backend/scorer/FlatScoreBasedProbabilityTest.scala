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
import common.rich.RichT.richT
import common.test.AuxSpecs

@Slow
class FlatScoreBasedProbabilityTest extends WordSpec with AuxSpecs with MockitoSugar {
  private val requiredProbability: ModelScore => Double = {
    case ModelScore.Default => requiredProbability(ModelScore.Okay)
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
        //  // TODO generalize, put in common
        //  val builder: ImmutableBiMap.Builder[ModelScore, Song] = ImmutableBiMap.builder()
        //  ModelScore.values.foreach(score => builder.put(score, mock[Song]))
        //  builder.build()
        //}
        def randomScore =
        // TODO scalacommon move to RichEnumertum
          ModelScore.values(Random.nextInt(ModelScore.values.length)).optFilter(_ != ModelScore.Default)
        val modelFactory = new FakeModelFactory
        val songs: Seq[Song] = Vector.fill(20000)(modelFactory.song())
        val songScores = songs.view.map(_.file: FileRef).map(_ -> randomScore).toMap
        object FakeModelScorer extends CachedModelScorer {
          override def apply(s: Song) = apply(s.file)
          override def apply(a: Artist) = ???
          override def apply(a: Album) = ???
          override def apply(f: FileRef) = songScores(f)
        }
        val allFiles = songScores.keys.toVector
        val $ = FlatScoreBasedProbability.withoutAsserts(
          requiredProbability,
          FakeModelScorer,
          allFiles,
          Logger.Empty,
        )
        val buffer = new ArrayBuffer[FileRef]()
        while (buffer.length < 1000) {
          // TODO move to RichSeq
          val nextSong: Song = songs(random.nextInt(allFiles.length))
          if ($(nextSong).roll(random))
            buffer += nextSong.file
        }
        // TODO ModelScore.orDefault, or figure a way to stop this this, e.g., remove Default to begin with
        // TODO generalize averageSatisfying?
        (buffer.count(songScores(_).getOrElse(ModelScore.Default) == score).toDouble / buffer.length) shouldBe requiredProbability(score) +- 0.05
      })
  }
}
