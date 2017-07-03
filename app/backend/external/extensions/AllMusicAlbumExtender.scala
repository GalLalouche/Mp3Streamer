package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.recon.Album

private object AllMusicAlbumExtender extends StaticExtender[Album] {
  override val host = Host.AllMusic
  override def apply[T <: Album](a: T, v: MarkedLink[T]): Seq[LinkExtension[T]] =
    appendSameSuffix(v, "similar")
}
