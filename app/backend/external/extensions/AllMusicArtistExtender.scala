package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.recon.Artist

private object AllMusicArtistExtender extends StaticExtender[Artist] {
  override val host = Host.AllMusic
  override def apply(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
    appendSameSuffix(v, "discography")
}
