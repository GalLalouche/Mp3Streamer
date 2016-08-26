package backend.external

import backend.Url
import common.rich.collections.RichTraversableOnce._

case class Host(name: String, url: Url) {
  import Host._
  def canonize: Host = hostsByName.getOrElse(name.toLowerCase, fromUrl(url))
}
object Host {
  private val hosts = Seq(Wikipedia, AllMusic, MetalArchives, MusicBrainz).mapBy(_.url)
  private val hostsByName = Seq(Wikipedia, AllMusic, MetalArchives, MusicBrainz, LastFm).mapBy(_.name.toLowerCase)
  object AllMusic extends Host("AllMusic", Url("www.allmusic.com"))
  object LastFm extends Host("LastFm", Url("http://www.last.fm/"))
  object MetalArchives extends Host("MetalArchives", Url("www.metal-archives.com"))
  object MusicBrainz extends Host("MusicBrainz", Url("musicbrainz.org"))
  object Wikipedia extends Host("Wikipedia", Url("en.wikipedia.org"))

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

