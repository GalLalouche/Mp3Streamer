package backend.external.extensions

import backend.external.MarkedLink
import backend.recon.Artist

private object LastFmArtistExtender extends LinkExtender[Artist] {
  override def apply[T <: Artist](a: T, v: MarkedLink[T]): Seq[LinkExtension[T]] =
    append(v, "similar" -> "+similar")
}
