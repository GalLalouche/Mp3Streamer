package backend.external.extensions

import backend.external.{ExternalLink, LinkExtension}
import backend.recon.Reconcilable

private object MusicBrainzExtender extends LinkExtender[Reconcilable] {
  override def apply[T <: Reconcilable](v: ExternalLink[T]): Seq[LinkExtension[T]] =
    append(v, "edit")
}
