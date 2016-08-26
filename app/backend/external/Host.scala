package backend.external

import backend.Url
import common.rich.collections.RichTraversableOnce._

case class Host(name: String, url: Url) {
  def canonize: Host = Host fromUrl url
}
object Host {
  private val hosts = Seq(Wikipedia, AllMusic, MetalArchives, MusicBrainz).mapBy(_.url)
  object Wikipedia extends Host("Wikipedia", Url("en.wikipedia.org"))
  object AllMusic extends Host("AllMusic", Url("www.allmusic.com"))
  object MetalArchives extends Host("MetalArchives", Url("www.metal-archives.com"))
  object MusicBrainz extends Host("MusicBrainz", Url("musicbrainz.org"))

  def fromUrl(url: Url): Host = {
    def extractHostName(u: Url) = u.address
        .toLowerCase
        .replaceAll("^https?://", "")
        .replaceAll("^www\\.", "")
        .takeWhile(_ != '.')
    val hostUrl = url.host
    hosts.getOrElse(hostUrl, Host(extractHostName(hostUrl), hostUrl))
  }
}

