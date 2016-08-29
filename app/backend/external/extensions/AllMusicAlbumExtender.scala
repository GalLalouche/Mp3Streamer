package backend.external.extensions

import backend.external.{ExternalLink, LinkExtension}
import backend.recon.Album

private object AllMusicAlbumExtender extends LinkExtender[Album] {
  override def apply[T <: Album](v: ExternalLink[T]): Seq[LinkExtension[T]] =
    append(v, "similar")
}
