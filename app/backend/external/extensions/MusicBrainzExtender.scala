package backend.external.extensions
import backend.external.{Host, MarkedLink}
import backend.recon.Reconcilable

abstract private class MusicBrainzExtender[R <: Reconcilable] extends StaticExtender[R] {
  override val host = Host.MusicBrainz
  override def apply(a: R, v: MarkedLink[R]): Seq[LinkExtension[R]] =
    appendSameSuffix(v, "edit") ++ SearchExtension.apply(Host.MusicBrainz, a).extensions
}
