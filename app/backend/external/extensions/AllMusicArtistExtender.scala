package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.recon.Artist

private object AllMusicArtistExtender extends StaticExtender[Artist] {
  override val host = Host.AllMusic
  override def apply[T <: Artist](a: T, v: MarkedLink[T]): Seq[LinkExtension[T]] =
    appendSameSuffix(v, "discography")
}
