package backend.external.extensions

import backend.configs.Configuration
import backend.external.{Host, MarkedLink}
import backend.recon.Album

private class AllMusicAlbumExtender(implicit c: Configuration) extends StaticExtender[Album] {
  override val host = Host.AllMusic
  override def apply(a: Album, v: MarkedLink[Album]): Seq[LinkExtension[Album]] =
    appendSameSuffix(v, "similar")
}
