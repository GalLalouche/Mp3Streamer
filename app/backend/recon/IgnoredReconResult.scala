package backend.recon

sealed trait IgnoredReconResult

object IgnoredReconResult {
  case object Ignored extends IgnoredReconResult
  case object NotIgnored extends IgnoredReconResult
  case object Missing extends IgnoredReconResult

  type IsIgnored = Boolean
  def from(o: Option[IsIgnored]): IgnoredReconResult = o match {
    case None => Missing
    case Some(b: IsIgnored) => if (b) Ignored else NotIgnored
  }
}
