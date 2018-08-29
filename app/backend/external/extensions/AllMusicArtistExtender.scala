package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.logging.Logger
import backend.recon.Artist
import javax.inject.Inject

private class AllMusicArtistExtender @Inject()(logger: Logger) extends StaticExtender[Artist](logger) {
  override val host = Host.AllMusic
  override def apply(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
    appendSameSuffix(v, "discography")
}
