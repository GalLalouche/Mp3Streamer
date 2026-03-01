package server

import backend.score.{ModelScore, ScoreBasedProbability, ScoreBasedProbabilityFactory}
import com.google.inject.{Module, Provides}
import models.Song
import net.codingwell.scalaguice.ScalaModule

import common.Percentage
import common.path.ref.FileRef

/**
 * The real ScoreBasedProbabilityFactory has assertions that fail with very few songs (expects a
 * distribution of scores across all ModelScore values). Tests that only need to verify endpoint
 * behavior can use this trivial implementation instead.
 */
private[server] object FakeScoreModule {
  val module: Module = new ScalaModule {
    @Provides private def scoreBasedProbabilityFactory: ScoreBasedProbabilityFactory =
      (_: Seq[FileRef]) =>
        new ScoreBasedProbability {
          override def apply(s: Song): Percentage = Percentage(0.5)
          override def apply(s: ModelScore): Percentage = Percentage(0.5)
        }
  }
}
