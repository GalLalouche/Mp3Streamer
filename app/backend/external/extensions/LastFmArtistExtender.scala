package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.logging.Logger
import backend.recon.Artist
import javax.inject.Inject

private class LastFmArtistExtender @Inject()(logger: Logger) extends StaticExtender[Artist](logger) {
  override val host = Host.LastFm
  override def apply(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
    append(v, "similar" -> "+similar")
}
