package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.recon.Artist

private object LastFmArtistExtender extends StaticExtender[Artist] {
  override val host = Host.LastFm
  override def apply[T <: Artist](a: T, v: MarkedLink[T]): Seq[LinkExtension[T]] =
    append(v, "similar" -> "+similar")
}
