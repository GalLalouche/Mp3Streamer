package backend.score

import common.path.ref.FileRef

// Could also be an interesting Software Design test question
trait ScoreBasedProbabilityFactory {
  def apply(songs: Seq[FileRef]): ScoreBasedProbability
}
