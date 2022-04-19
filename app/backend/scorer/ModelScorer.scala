package backend.scorer

import backend.FutureOption
import models.Song

trait ModelScorer {
  def apply(s: Song): FutureOption[ModelScore]
}
