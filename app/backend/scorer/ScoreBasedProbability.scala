package backend.scorer

import common.Percentage
import models.Song

/** Returns a [[ModelScore]] based weight for the song to be chosen. */
trait ScoreBasedProbability {
  def apply(s: Song): Percentage
  def apply(s: ModelScore): Percentage
}
