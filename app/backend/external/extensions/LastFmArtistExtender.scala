package backend.external.extensions

import javax.inject.Inject

import backend.external.{Host, MarkedLink}
import backend.recon.Artist

private class LastFmArtistExtender @Inject() (helper: StaticExtenderHelper)
    extends LinkExtender[Artist] {
  override val host = Host.LastFm
  private val staticExtender: StaticExtender[Artist] = new StaticExtender[Artist] {
    override def host = LastFmArtistExtender.this.host
    override def extend(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
      append(v, "similar" -> "+similar")
  }

  override def extend = helper(staticExtender)
}
