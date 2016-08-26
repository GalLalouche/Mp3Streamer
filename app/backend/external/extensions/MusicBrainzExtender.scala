package backend.external.extensions

import backend.external.{ExternalLink, LinkExtensions}
import backend.recon.Reconcilable

object MusicBrainzExtender extends LinkExtender[Reconcilable] {
  override def apply[T <: Reconcilable](v: ExternalLink[T]): Seq[LinkExtensions[T]] =
    Seq(LinkExtensions[T]("edit", v.link + "/edit"))
}
