package backend.scorer

import backend.recon.{Album, Artist}
import models.Song

import common.io.FileRef

// TODO interface segregation, not all scorers need this
// TODO SoftwareDesign could be an interesting question
trait CachedModelScorer {
  def apply(a: Artist): Option[ModelScore]
  def apply(a: Album): Option[ModelScore]
  def apply(s: Song): Option[ModelScore]
  /**
   * This method should avoid parsing songs. Instead, it should extract the necessary metadata from
   * the file path. This makes it safe to call while traversing a (very large) list of files.
   */
  def apply(f: FileRef): Option[ModelScore]
  def fullInfo(s: Song): FullInfoScore
}
