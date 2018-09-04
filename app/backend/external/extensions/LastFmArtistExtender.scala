package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.recon.Artist
import javax.inject.Inject

private class LastFmArtistExtender @Inject()(helper: StaticExtenderHelper) extends LinkExtender[Artist] {
  override val host = Host.LastFm
  private val staticExtender: StaticExtender[Artist] = new StaticExtender[Artist] {
    override def host = LastFmArtistExtender.this.host
    override def extend(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
      append(v, "similar" -> "+similar")
  }

  override def extend = helper(staticExtender)
}
