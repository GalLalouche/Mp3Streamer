package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.recon.Artist
import javax.inject.Inject

private class AllMusicArtistExtender @Inject()(helper: StaticExtenderHelper) extends LinkExtender[Artist] {
  override val host = Host.AllMusic
  private val staticExtender: StaticExtender[Artist] = new StaticExtender[Artist] {
    override def host = AllMusicArtistExtender.this.host
    override def extend(a: Artist, v: MarkedLink[Artist]): Seq[LinkExtension[Artist]] =
      appendSameSuffix(v, "discography")
  }

  override def extend = helper(staticExtender)
}
