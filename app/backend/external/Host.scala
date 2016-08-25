package backend.external

import backend.Url
import common.rich.collections.RichTraversableOnce._

case class Host(name: String, url: Url)
object Host {
  def fromUrl(url: Url): Host = {
    def extractHostName(u: Url) = u.address
        .replaceAll("^https?://", "")
        .replaceAll("^www\\.", "")
        .takeWhile(_ != '.')
    val hostUrl = url.host
    hosts.getOrElse(hostUrl, Host(extractHostName(hostUrl), hostUrl))

  }

  private val hosts = Seq(Wikipedia, AllMusic, MetalArchives).mapBy(_.url)
  object Wikipedia extends Host("wikipedia", Url("en.wikipedia.org"))
  object AllMusic extends Host("allMusic", Url("www.allmusic.com"))
  object MetalArchives extends Host("metalarchives", Url("www.metal-archives.com"))
  object MusicBrainz extends Host("musicbrainz", Url("musicbrainz.org"))
}

