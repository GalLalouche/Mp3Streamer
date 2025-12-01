package backend.score

import backend.recon.Track

private trait FullInfoScorer {
  def fullInfo(s: Track): FullInfoScore
}
