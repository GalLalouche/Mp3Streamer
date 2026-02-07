package backend.score

import backend.recon.Reconcilable.SongExtractor
import backend.score.OptionalModelScore.Default
import com.google.inject.Singleton
import models.Song

import common.Percentage
import common.path.ref.FileRef
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichDouble.richDouble

/** Returns a [[ModelScore]] based weight for the song to be chosen. */
@Singleton private class FlatScoreBasedProbability private (
    defaultScore: Percentage,
    scorer: AggregateScorer,
    probabilities: Map[ModelScore, Percentage],
) extends ScoreBasedProbability {
  def apply(s: Song): Percentage =
    scorer.aggregateScore(s.track).toModelScore.fold(defaultScore)(apply)
  def apply(score: ModelScore): Percentage = probabilities(score)
}

private object FlatScoreBasedProbability {
  // TODO SoftwareDesign could be an interesting guice question about passing configuration
  // The basic math here is this: given a target distribution of score probabilities, we have an
  // observed score distribution in the library, we want to adjust the probabilities
  // of selecting songs so that the resulting distribution of song scores matches the target
  // distribution.
  // To do this, we can compute a weight for each score that is proportional to the ratio of the
  // target probability to the observed probability. To actually select a song, we first select a
  // random song, then keep it // with probability equal to the weight for its score. Repeat until
  // a song is kept.
  // A little toy example: suppose there are two scores, X and Y, with target probabilities of 0.2
  // and 0.8, respectively, and observed frequencies of 0.4 and 0.6. Then, the weight for X would be
  // 0.2 / 0.4 = 0.5, and the weight for Y would be 0.8 / 0.6 = 1.333. Since this can lead to
  // weights that don't sum to 1.0 (1.333 in our example), so we need to normalize them by dividing
  // each weight by the sum of all weights, i.e., 0.5 + 1.333 = 1.833 in this case. So the
  // normalized weight for score X would be 0.5 / 1.833 = 0.273, and the normalized weight for score
  // Y would be 1.333 / 1.833 = 0.727. Thus X would be chosen with probability 0.4 * 0.273 = 0.109,
  // and Y would be chosen with probability 0.6 * 0.727 = 0.436, i.e., 4 times as often, matching
  // the target distribution.
  def apply(
      target: ModelScore => Double,
      defaultScore: Percentage,
      scorer: AggregateScorer,
      songFiles: Seq[FileRef],
      withAsserts: Boolean,
  ): FlatScoreBasedProbability = {
    { // Validate input sum.
      val totalSum: Double = ModelScore.values.map(target).sum
      require(
        totalSum.isRoughly(1, 0.0000001),
        s"total sum was <$totalSum>, which is probably a bug",
      )
    }

    // TODO SoftwareDesign could be an interesting question about reducing memory usage by replacing
    //  a constructor field with a factory one.
    val probabilities: Map[ModelScore, Percentage] = {
      // We can't extract tracks from some file paths (Classical and Musical predominantly), so we
      // ignore those, since parsing the ID3 of all those tags would be too slow, and it doesn't
      // affect the probability distribution all that much, based on benchmarks (about 0.01
      // difference in some of the probabilities).
      val optionalFrequencies: Map[Option[OptionalModelScore], Int] = songFiles.view
        .map(scorer.tryAggregateScore(_).map(_.toOptionalModelScore))
        .frequencies

      val total = optionalFrequencies.values.sum

      { // Summarize ignored and default scores.
        def summary(c: Int) = f"<$c> / <$total> (${100.0 * c.toDouble / total}%.2f%%)"

        scribe.debug(
          f"Found ${summary(optionalFrequencies.getOrElse(Some(Default), 0))} Default scores",
        )
        val noneCount = optionalFrequencies.getOrElse(None, 0)
        scribe.debug(s"${summary(noneCount)} file scores ignored due to invalid paths.")
      }

      val observed: Map[ModelScore, Int] =
        optionalFrequencies
          // We're ignoring Default scores; those will have const probability of <defaultScore>.
          .collect { case (Some(OptionalModelScore.Scored(s)), count) => s -> count }
          .mapIf(_.isEmpty)
          .to(Map((ModelScore.Okay: ModelScore) -> 1))
      val unnormalized = observed.map { case (score, count) =>
        score -> target(score) / (count.toDouble / total)
      }
      val unnormalizedSum = unnormalized.values.sum
      val $ = unnormalized.view.mapValues(_ / unnormalizedSum).mapValues(Percentage.apply).toMap

      { // Validate sum reaches 1.0 after normalization.
        val sum = $.values.view.map(_.p).sum
        assert($.isEmpty || sum.isRoughly(1.0), s"Expected values to sum to 1.0, was: <$sum>")
      }

      def baseProbability(score: ModelScore) = observed(score) / total.toDouble

      def debugMessage(score: ModelScore): Unit =
        scribe.debug(
          s"Base probability for <$score> was <${baseProbability(score)}>, " +
            s"required is <${target(score)}>, normalized probability is <${$(score)}>",
        )

      def assertReducedProbability(score: ModelScore): Unit = {
        debugMessage(score)
        if (withAsserts)
          assert(
            baseProbability(score) > target(score),
            s"${baseProbability(score)} <= ${target(score)}",
          )
      }

      def assertIncreasedProbability(score: ModelScore): Unit = {
        debugMessage(score)
        if (withAsserts)
          assert(
            baseProbability(score) < target(score),
            s"${baseProbability(score)} >= ${target(score)}",
          )
      }

      assertReducedProbability(ModelScore.Crappy)
      assertReducedProbability(ModelScore.Meh)
      // 'Okay' has no inherent bias, though it'll probably be lower to accommodate the good scores.
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
      scorer: AggregateScorer,
      songFiles: Seq[FileRef],
  ): FlatScoreBasedProbability = apply(map, defaultScore, scorer, songFiles, withAsserts = true)

  private[score] def withoutAsserts(
      map: ModelScore => Double,
      defaultScore: Percentage,
      scorer: AggregateScorer,
      songFiles: Seq[FileRef],
  ): FlatScoreBasedProbability = apply(map, defaultScore, scorer, songFiles, withAsserts = false)
}
