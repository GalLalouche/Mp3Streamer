package backend.external.extensions

import backend.configs.Configuration
import backend.external.{Host, MarkedLink}
import backend.recon.Artist

private class LastFmArtistExtender(implicit c: Configuration) extends StaticExtender[Artist] {
  override val host = Host.LastFm
  override def apply(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
    append(v, "similar" -> "+similar")
}
