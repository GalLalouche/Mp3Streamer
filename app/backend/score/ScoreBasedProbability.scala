package backend.score

import backend.recon.Track

import common.Percentage

/** Returns a [[ModelScore]] based weight for the song to be chosen. */
trait ScoreBasedProbability {
  def apply(t: Track): Percentage
  def apply(s: ModelScore): Percentage
}
