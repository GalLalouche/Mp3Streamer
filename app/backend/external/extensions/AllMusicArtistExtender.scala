package backend.external.extensions

import backend.external.BaseLink
import backend.recon.Artist

private object AllMusicArtistExtender extends LinkExtender[Artist] {
  override def apply[T <: Artist](a: T, v: BaseLink[T]): Seq[LinkExtension[T]] =
    appendSameSuffix(v, "discography")
}
