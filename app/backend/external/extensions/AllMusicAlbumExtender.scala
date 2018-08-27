package backend.external.extensions

import backend.configs.Configuration
import backend.external.{Host, MarkedLink}
import backend.logging.Logger
import backend.recon.Album
import net.codingwell.scalaguice.InjectorExtensions._

private class AllMusicAlbumExtender(implicit c: Configuration)
    extends StaticExtender[Album](c.injector.instance[Logger]) {
  override val host = Host.AllMusic
  override def apply(a: Album, v: MarkedLink[Album]): Seq[LinkExtension[Album]] =
    appendSameSuffix(v, "similar")
}
