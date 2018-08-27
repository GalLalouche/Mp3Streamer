package backend.external.extensions

import backend.configs.Configuration
import backend.external.{Host, MarkedLink}
import backend.logging.Logger
import backend.recon.Artist
import net.codingwell.scalaguice.InjectorExtensions._

private class LastFmArtistExtender(implicit c: Configuration)
    extends StaticExtender[Artist](c.injector.instance[Logger]) {
  override val host = Host.LastFm
  override def apply(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
    append(v, "similar" -> "+similar")
}
