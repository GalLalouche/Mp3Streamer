package backend.recon

sealed trait StoredReconResult {
  def isIgnored: Boolean
}

object StoredReconResult {
  case object NoRecon extends StoredReconResult {
    override val isIgnored = true
  }
  // TODO better name for isIgnored
  case class HasReconResult(reconId: ReconID, override val isIgnored: Boolean)
      extends StoredReconResult {
    def ignored: HasReconResult = copy(isIgnored = true)
  }

  def unignored(reconId: ReconID): HasReconResult = HasReconResult(reconId, isIgnored = false)
}
