package backend.external.extensions

import backend.external.BaseLink
import backend.recon.Album

private object AllMusicAlbumExtender extends LinkExtender[Album] {
  override def apply[T <: Album](a: T, v: BaseLink[T]): Seq[LinkExtension[T]] =
    appendSameSuffix(v, "similar")
}
