package backend.external.extensions

import javax.inject.Inject

import backend.external.{Host, MarkedLink}
import backend.recon.Album

private class AllMusicAlbumExtender @Inject() (helper: StaticExtenderHelper)
    extends LinkExtender[Album] {
  override val host = Host.AllMusic
  private val staticExtender: StaticExtender[Album] = new StaticExtender[Album] {
    override def host = AllMusicAlbumExtender.this.host
    override def extend(a: Album, v: MarkedLink[Album]): Seq[LinkExtension[Album]] =
      appendSameSuffix(v, "similar")
  }
  override def extend = helper(staticExtender)
}
