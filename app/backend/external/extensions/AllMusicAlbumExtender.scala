package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.recon.Album

private object AllMusicAlbumExtender extends StaticExtender[Album] {
  override val host = Host.AllMusic
  override def apply(a: Album, v: MarkedLink[Album]): Seq[LinkExtension[Album]] =
    appendSameSuffix(v, "similar")
}
