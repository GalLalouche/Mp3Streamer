package backend.score

import backend.recon.Track
import backend.score.model.FullInfoScore

private trait FullInfoScorer {
  def fullInfo(s: Track): FullInfoScore
}
