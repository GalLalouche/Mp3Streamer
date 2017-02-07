package backend.external.extensions

import backend.external.ExternalLink
import backend.recon.Artist

private object LastFmArtistExtender extends LinkExtender[Artist] {
  override def apply[T <: Artist](a: T, v: ExternalLink[T]): Seq[LinkExtension[T]] =
    append(v, "Similar" -> "+similar")
}
