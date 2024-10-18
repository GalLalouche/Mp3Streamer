package backend.scorer

import backend.recon.{Album, Artist}
import models.Song

import common.io.FileRef

// TODO interface segregation, not all scorers need this
// TODO SoftwareDesign could be an interesting question
trait CachedModelScorer {
  // Item scores is the actual score set for the specific item, i.e., ignoring parent scores on
  // default.
  def explicitScore(a: Artist): OptionalModelScore
  def explicitScore(a: Album): OptionalModelScore
  def explicitScore(s: Song): OptionalModelScore
  /**
   * Gets the final score for the song. If the song has no score, it will return the result from
   * album or artist, depending on the first one that has a score.
   */
  def aggregateScore(s: Song): OptionalModelScore = fullInfo(s).toOptionalModelScore
  /**
   * This method should avoid parsing songs. Instead, it should extract the necessary metadata from
   * the file path. This makes it safe to call while traversing a (very large) list of files.
   */
  def aggregateScore(f: FileRef): OptionalModelScore
  def fullInfo(s: Song): FullInfoScore
}
