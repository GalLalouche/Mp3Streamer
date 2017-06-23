package backend.external.extensions

import backend.external.{ExternalLink, Host}
import backend.recon.Reconcilable

private object MusicBrainzExtender extends LinkExtender[Reconcilable] {
  override def apply[T <: Reconcilable](a: T, v: ExternalLink[T]): Seq[LinkExtension[T]] =
    appendSameSuffix(v, "edit") ++ SearchExtension.apply(Host.MusicBrainz, a).extensions
}
