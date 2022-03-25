package backend.scorer

import models.Song

import common.Percentage

/** Returns a [[ModelScore]] based weight for the song to be chosen. */
trait ScoreBasedProbability {
  // TODO percentage of score makes more sense than that of a song, and would make the implementation simpler
  def apply(s: Song): Percentage
}
