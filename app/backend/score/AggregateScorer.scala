package backend.score

import backend.recon.Track

import common.io.FileRef

trait AggregateScorer {
  /**
   * This method should avoid parsing songs. Instead, it should extract the necessary metadata from
   * the file path. This makes it safe to call while traversing a (very large) list of files.
   */
  def aggregateScore(f: FileRef): SourcedOptionalModelScore
  /**
   * Gets the final score for the song. If the song has no score, it will return the result from
   * album or artist, depending on the first one that has a score.
   */
  def aggregateScore(s: Track): SourcedOptionalModelScore
}
