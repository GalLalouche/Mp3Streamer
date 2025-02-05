package backend.scorer

import javax.inject.Singleton

import backend.recon.Reconcilable.SongExtractor
import models.Song

import common.Percentage
import common.io.FileRef
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichDouble.richDouble

/** Returns a [[ModelScore]] based weight for the song to be chosen. */
// TODO SoftwareDesign could be an interesting guice question about passing configuration
@Singleton private class FlatScoreBasedProbability private (
    map: ModelScore => Double,
    defaultScore: Percentage,
    scorer: CachedModelScorer,
    songFiles: IndexedSeq[FileRef],
    withAsserts: Boolean,
) extends ScoreBasedProbability {
  {
    val totalSum: Double = ModelScore.values.map(map).sum
    require(totalSum.isRoughly(1, 0.0000001), s"total sum was <$totalSum>, which is probably a bug")
  }
  private val probabilities: Map[ModelScore, Percentage] =
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
      val $ = unnormalized.mapValues(_ / unnormalizedSum).mapValues(Percentage.apply).view.force;

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
          assert(baseProbability(score) < map(score), s"${baseProbability(score)} >= ${map(score)}")
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
  def apply(s: Song): Percentage =
    scorer.aggregateScore(s.track).toModelScore.fold(defaultScore)(apply)
  def apply(score: ModelScore): Percentage = probabilities(score)
}

private object FlatScoreBasedProbability {
  def withAsserts(
      map: ModelScore => Double,
      defaultScore: Percentage,
      scorer: CachedModelScorer,
      songFiles: IndexedSeq[FileRef],
  ) = new FlatScoreBasedProbability(
    map,
    defaultScore,
    scorer,
    songFiles,
    withAsserts = true,
  )

  private[scorer] def withoutAsserts(
      map: ModelScore => Double,
      defaultScore: Percentage,
      scorer: CachedModelScorer,
      songFiles: IndexedSeq[FileRef],
  ): FlatScoreBasedProbability = new FlatScoreBasedProbability(
    map,
    defaultScore,
    scorer,
    songFiles,
    withAsserts = false,
  )
}
