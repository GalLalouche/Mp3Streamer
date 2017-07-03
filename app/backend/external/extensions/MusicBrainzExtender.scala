package backend.external.extensions

import backend.external.{MarkedLink, Host}
import backend.recon.Reconcilable

private object MusicBrainzExtender extends StaticExtender[Reconcilable] {
  override val host = Host.MusicBrainz
  override def apply[T <: Reconcilable](a: T, v: MarkedLink[T]): Seq[LinkExtension[T]] =
    appendSameSuffix(v, "edit") ++ SearchExtension.apply(Host.MusicBrainz, a).extensions
}
