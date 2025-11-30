package backend.score

import backend.recon.Track

trait FullInfoScorer {
  def fullInfo(s: Track): FullInfoScore
}
