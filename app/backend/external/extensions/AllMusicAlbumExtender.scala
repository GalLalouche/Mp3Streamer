package backend.external.extensions

import backend.external.MarkedLink
import backend.recon.Album

private object AllMusicAlbumExtender extends LinkExtender[Album] {
  override def apply[T <: Album](a: T, v: MarkedLink[T]): Seq[LinkExtension[T]] =
    appendSameSuffix(v, "similar")
}
