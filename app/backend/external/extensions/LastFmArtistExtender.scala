package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.recon.Artist

private object LastFmArtistExtender extends StaticExtender[Artist] {
  override val host = Host.LastFm
  override def apply(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
    append(v, "similar" -> "+similar")
}
