package backend.score

import backend.recon.{Album, Artist, Track}

/**
 * Item scores is the actual score set for the specific item, i.e., ignoring parent scores on
 * default.
 */
trait IndividualScorer {
  def explicitScore(a: Artist): OptionalModelScore
  def explicitScore(a: Album): OptionalModelScore
  def explicitScore(t: Track): OptionalModelScore
}
