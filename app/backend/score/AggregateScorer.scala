package backend.score

import backend.recon.Track

import common.path.ref.FileRef

trait AggregateScorer {
  /**
   * Only extracts the score if the file metadata can be extracted without resorting to parsing ID3
   * attributes. If it can't, returns [[None]]. This makes it safe to call while traversing a (very
   * large) list of files.
   */
  def tryAggregateScore(f: FileRef): Option[SourcedOptionalModelScore]
  /**
   * Gets the final score for the song. If the song has no score, it will return the result from
   * album or artist, depending on the first one that has a score.
   */
  def aggregateScore(s: Track): SourcedOptionalModelScore
}
