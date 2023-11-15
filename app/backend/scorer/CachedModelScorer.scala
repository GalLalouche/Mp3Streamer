package backend.scorer

import backend.recon.{Album, Artist}
import common.io.FileRef
import models.Song

// TODO interface segregation, not all scorers need this
// TODO SoftwareDesign could be an interesting question
trait CachedModelScorer {
  def apply(a: Artist): OptionalModelScore
  def apply(a: Album): OptionalModelScore
  def apply(s: Song): OptionalModelScore
  /**
   * This method should avoid parsing songs. Instead, it should extract the necessary metadata from
   * the file path. This makes it safe to call while traversing a (very large) list of files.
   */
  def apply(f: FileRef): OptionalModelScore
  def fullInfo(s: Song): FullInfoScore
}
