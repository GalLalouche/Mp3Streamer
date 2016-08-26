package backend.external.extensions

import backend.external.{ExternalLink, LinkExtensions}
import backend.recon.Artist

private object AllMusicArtistExtender extends LinkExtender[Artist] {
  override def apply[T <: Artist](v: ExternalLink[T]): Seq[LinkExtensions[T]] =
    Seq(LinkExtensions[T]("discography", v.link + "/discography"))
}
