package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.logging.LoggerProvider
import backend.recon.Artist

private class LastFmArtistExtender(implicit lp: LoggerProvider) extends StaticExtender[Artist] {
  override val host = Host.LastFm
  override def apply(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
    append(v, "similar" -> "+similar")
}
