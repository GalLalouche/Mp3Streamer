package backend.external.extensions

import javax.inject.Inject

import backend.external.{Host, MarkedLink}
import backend.recon.Artist

private class AllMusicArtistExtender @Inject() (helper: StaticExtenderHelper)
    extends LinkExtender[Artist] {
  override val host = Host.AllMusic
  private val staticExtender: StaticExtender[Artist] = new StaticExtender[Artist] {
    override def host = AllMusicArtistExtender.this.host
    override def extend(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
      appendSameSuffix(v, "discography")
  }

  override def extend = helper(staticExtender)
}
