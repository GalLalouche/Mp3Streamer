package backend.external.extensions

import backend.external.ExternalLink
import backend.recon.Artist

private object AllMusicArtistExtender extends LinkExtender[Artist] {
  override def apply[T <: Artist](v: ExternalLink[T]): Seq[LinkExtension[T]] =
    append(v, "discography")
}
