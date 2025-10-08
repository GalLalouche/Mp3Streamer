package backend.score

import backend.recon.Reconcilable.SongExtractor
import com.google.inject.Singleton
import models.Song

import common.Percentage
import common.io.FileRef
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichDouble.richDouble

/** Returns a [[ModelScore]] based weight for the song to be chosen. */
@Singleton private class FlatScoreBasedProbability private (
    defaultScore: Percentage,
    scorer: CachedModelScorer,
    probabilities: Map[ModelScore, Percentage],
) extends ScoreBasedProbability {
  def apply(s: Song): Percentage =
    scorer.aggregateScore(s.track).toModelScore.fold(defaultScore)(apply)
  def apply(score: ModelScore): Percentage = probabilities(score)
}

private object FlatScoreBasedProbability {
  // TODO SoftwareDesign could be an interesting guice question about passing configuration
  def apply(
      map: ModelScore => Double,
      defaultScore: Percentage,
      scorer: CachedModelScorer,
      songFiles: IndexedSeq[FileRef],
      withAsserts: Boolean,
  ): FlatScoreBasedProbability = {
    {
      val totalSum: Double = ModelScore.values.map(map).sum
      require(
        totalSum.isRoughly(1, 0.0000001),
        s"total sum was <$totalSum>, which is probably a bug",
      )
    }
    // TODO SoftwareDesign could be an interesting question about reducing memory usage by replacing
    //  a constructor field with a factory one.
    val probabilities: Map[ModelScore, Percentage] =
      // songFiles can be empty in controller tests.
      if (songFiles.isEmpty) Map()
      else {
        val sum = songFiles.length
        val frequencies: Map[ModelScore, Int] = songFiles
          .flatMap(scorer.aggregateScore(_).toModelScore)
          .frequencies
        val unnormalized = frequencies.map { case (score, count) =>
          score -> map(score) / (count.toDouble / sum)
        }
        val unnormalizedSum = unnormalized.values.sum
        val $ = unnormalized.view.mapValues(_ / unnormalizedSum).mapValues(Percentage.apply).toMap;

        {
          val sum = $.values.view.map(_.p).sum
          assert($.isEmpty || sum.isRoughly(1.0), s"Expected values to sum to 1.0, was: <$sum>")
        }

        def baseProbability(score: ModelScore) = frequencies(score) / songFiles.size.toDouble

        def debugMessage(score: ModelScore): Unit =
          scribe.debug(
            s"Base probability for <$score> was <${baseProbability(score)}>, " +
              s"required is <${map(score)}>, normalized probability is <${$(score)}>",
          )

        def assertReducedProbability(score: ModelScore): Unit = {
          debugMessage(score)
          if (withAsserts)
            assert(baseProbability(score) > map(score))
        }

        def assertIncreasedProbability(score: ModelScore): Unit = {
          debugMessage(score)
          if (withAsserts)
            assert(
              baseProbability(score) < map(score),
              s"${baseProbability(score)} >= ${map(score)}",
            )
        }

        assertReducedProbability(ModelScore.Crappy)
        assertReducedProbability(ModelScore.Meh)
        // Okay has no inherent bias, though it'll probably be lower to accommodate the good scores.
        debugMessage(ModelScore.Okay)
        assertIncreasedProbability(ModelScore.Good)
        assertIncreasedProbability(ModelScore.Great)
        assertIncreasedProbability(ModelScore.Amazing)
        $
      }
    new FlatScoreBasedProbability(defaultScore, scorer, probabilities)
  }

  def withAsserts(
      map: ModelScore => Double,
      defaultScore: Percentage,
      scorer: CachedModelScorer,
      songFiles: IndexedSeq[FileRef],
  ): FlatScoreBasedProbability = apply(map, defaultScore, scorer, songFiles, withAsserts = true)

  private[score] def withoutAsserts(
      map: ModelScore => Double,
      defaultScore: Percentage,
      scorer: CachedModelScorer,
      songFiles: IndexedSeq[FileRef],
  ): FlatScoreBasedProbability = apply(map, defaultScore, scorer, songFiles, withAsserts = false)
}
