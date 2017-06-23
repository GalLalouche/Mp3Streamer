package backend.external.extensions

import backend.external.ExternalLink
import backend.recon.Album

private object AllMusicAlbumExtender extends LinkExtender[Album] {
  override def apply[T <: Album](a: T, v: ExternalLink[T]): Seq[LinkExtension[T]] =
    appendSameSuffix(v, "similar")
}
