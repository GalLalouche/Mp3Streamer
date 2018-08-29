package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.logging.Logger
import backend.recon.Album
import javax.inject.Inject

private class AllMusicAlbumExtender @Inject()(logger: Logger) extends StaticExtender[Album](logger) {
  override val host = Host.AllMusic
  override def apply(a: Album, v: MarkedLink[Album]): Seq[LinkExtension[Album]] =
    appendSameSuffix(v, "similar")
}
