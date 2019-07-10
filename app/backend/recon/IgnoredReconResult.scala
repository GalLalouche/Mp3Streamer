package backend.recon

sealed trait IgnoredReconResult

object IgnoredReconResult {
  case object Ignored extends IgnoredReconResult
  case object NotIgnored extends IgnoredReconResult
  case object Missing extends IgnoredReconResult

  def from(o: Option[Boolean]): IgnoredReconResult = o match {
    case None => Missing
    case Some(b) => if (b) Ignored else NotIgnored
  }
}
