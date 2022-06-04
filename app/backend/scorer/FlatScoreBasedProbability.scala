package backend.scorer

import backend.logging.Logger
import backend.scorer.FlatScoreBasedProbability.withoutAsserts
import javax.inject.Singleton
import models.Song

import common.Percentage
import common.io.FileRef
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichDouble.richDouble

/** Returns a [[ModelScore]] based weight for the song to be chosen. */
// TODO SoftwareDesign could be an interesting guice question about passing configuration
@Singleton private class FlatScoreBasedProbability private(
    map: ModelScore => Double,
    defaultScore: Percentage,
    scorer: CachedModelScorer,
    songFiles: Seq[FileRef],
    logger: Logger,
    withAsserts: Boolean,
) extends ScoreBasedProbability {
  {
    val totalSum: Double = ModelScore.values.map(map).sum
    require(totalSum.isRoughly(1, 0.0000001), s"total sum was <$totalSum>, which is probably a bug")
  }
  private val probabilities: Map[ModelScore, Percentage] = {
    val sum = songFiles.length
    val frequencies: Map[ModelScore, Int] = songFiles
        .flatMap(scorer(_))
        .frequencies
    val unnormalized = frequencies.map {case (score, count) =>
      score -> map(score) / (count.toDouble / sum)
    }
    val unnormalizedSum = unnormalized.values.sum
    val $ = unnormalized.mapValues(_ / unnormalizedSum).mapValues(Percentage.apply).view.force
    assert($.values.view.map(_.p).sum isRoughly 1.0)
    def baseProbability(score: ModelScore) = frequencies(score) / songFiles.size.toDouble
    def debugMessage(score: ModelScore): Unit =
      logger.debug(s"Base probability for <$score> was <${baseProbability(score)}>, " +
          s"required is <${map(score)}>, normalized probability is <${$(score)}>")
    def assertReducedProbability(score: ModelScore): Unit = {
      debugMessage(score)
      if (withAsserts)
        assert(baseProbability(score) > map(score))
    }
    def assertIncreasedProbability(score: ModelScore): Unit = {
      debugMessage(score)
      if (withAsserts)
        assert(baseProbability(score) < map(score))
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
  def apply(s: Song): Percentage = scorer(s).fold(defaultScore)(apply)
  def apply(score: ModelScore): Percentage = probabilities(score)
}

private object FlatScoreBasedProbability {
  def withAsserts(
      map: ModelScore => Double,
      defaultScore: Percentage,
      scorer: CachedModelScorer,
      songFiles: Seq[FileRef],
      logger: Logger
  ) = new FlatScoreBasedProbability(
    map,
    defaultScore,
    scorer,
    songFiles,
    logger,
    withAsserts = true,
  )

  private[scorer] def withoutAsserts(
      map: ModelScore => Double,
      defaultScore: Percentage,
      scorer: CachedModelScorer,
      songFiles: Seq[FileRef],
      logger: Logger,
  ): FlatScoreBasedProbability = new FlatScoreBasedProbability(
    map,
    defaultScore,
    scorer,
    songFiles,
    logger,
    withAsserts = false,
  )
}
