package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.logging.LoggerProvider
import backend.recon.Album

private class AllMusicAlbumExtender(implicit lp: LoggerProvider) extends StaticExtender[Album] {
  override val host = Host.AllMusic
  override def apply(a: Album, v: MarkedLink[Album]): Seq[LinkExtension[Album]] =
    appendSameSuffix(v, "similar")
}
