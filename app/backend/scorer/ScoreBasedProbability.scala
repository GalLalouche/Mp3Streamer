package backend.scorer

import models.Song

import common.Percentage

/** Returns a [[ModelScore]] based weight for the song to be chosen. */
trait ScoreBasedProbability {
  def apply(s: Song): Percentage
  def apply(s: ModelScore): Percentage
}
