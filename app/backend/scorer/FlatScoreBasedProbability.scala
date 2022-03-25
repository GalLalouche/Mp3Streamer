package backend.scorer

import backend.logging.Logger
import javax.inject.Singleton
import models.Song

import common.Percentage
import common.io.FileRef
import common.rich.collections.RichTraversableOnce.richTraversableOnce

/** Returns a [[ModelScore]] based weight for the song to be chosen. */
// TODO SoftwareDesign could be an interesting guice question about passing configuration
@Singleton private class FlatScoreBasedProbability private(
    map: ModelScore => Double,
    scorer: CachedModelScorer,
    songFiles: Seq[FileRef],
    logger: Logger,
    withAsserts: Boolean,
) extends ScoreBasedProbability {
  private val probabilities: Map[ModelScore, Double] = {
    val sum = songFiles.length
    val frequencies: Map[ModelScore, Int] = songFiles
        .map(scorer(_) getOrElse ModelScore.Default)
        .frequencies
    val unnormalized = frequencies.map {case (score, count) =>
      score -> map(score) / (count.toDouble / sum)
    }
    val unnormalizedSum = unnormalized.values.sum
    val $ = unnormalized.mapValues(_ / unnormalizedSum)
    def baseProbability(score: ModelScore) = frequencies(score) / songFiles.size.toDouble
    def debugMessage(score: ModelScore): Unit =
      logger.debug(s"Base probability for <$score> was <${baseProbability(score)}>, " +
          s"required is ${map(score)}")
    def assertReducedProbability(score: ModelScore): Unit = {
      debugMessage(score)
      assert(baseProbability(score) > map(score))
    }
    def assertIncreasedProbability(score: ModelScore): Unit = {
      debugMessage(score)
      assert(baseProbability(score) < map(score))
    }

    if (withAsserts) {
      assertReducedProbability(ModelScore.Crappy)
      assertReducedProbability(ModelScore.Meh)
      // Okay has no inherent bias, though it'll probably be lower to accommodate the good scores.
      debugMessage(ModelScore.Okay)
      assertIncreasedProbability(ModelScore.Good)
      assertIncreasedProbability(ModelScore.Great)
      assertIncreasedProbability(ModelScore.Amazing)
    }
    $
  }
  def apply(s: Song): Percentage = probabilities(scorer(s).getOrElse(ModelScore.Default))
}

private object FlatScoreBasedProbability {
  def withAsserts(
      map: ModelScore => Double,
      scorer: CachedModelScorer,
      songFiles: Seq[FileRef],
      logger: Logger
  ) = new FlatScoreBasedProbability(
    map,
    scorer: CachedModelScorer,
    songFiles,
    logger,
    withAsserts = true,
  )

  private[scorer] def withoutAsserts(
      map: ModelScore => Double,
      scorer: CachedModelScorer,
      songFiles: Seq[FileRef],
      logger: Logger,
  ): FlatScoreBasedProbability = new FlatScoreBasedProbability(
    map: ModelScore => Double,
    scorer: CachedModelScorer,
    songFiles: Seq[FileRef],
    logger,
    withAsserts = false,
  )
}
