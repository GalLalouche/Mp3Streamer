package backend.external

import backend.Url
import common.rich.collections.RichTraversableOnce._

case class Host(name: String, url: Url) {
  import Host._
  def canonize: Host = hostsByName.getOrElse(name.toLowerCase.replaceAll("[*?]$", ""), defaultFor(url))
}

object Host {
  object AllMusic extends Host("AllMusic", Url("www.allmusic.com"))
  object Facebook extends Host("Facebook", Url("www.facebook.com"))
  object LastFm extends Host("LastFm", Url("www.last.fm"))
  object MetalArchives extends Host("MetalArchives", Url("www.metal-archives.com"))
  object MusicBrainz extends Host("MusicBrainz", Url("musicbrainz.org"))
  object RateYourMusic extends Host("RateYourMusic", Url("rateyourmusic.com"))
  object Wikipedia extends Host("Wikipedia", Url("en.wikipedia.org"))
  val hosts = Vector(
    AllMusic,
    Facebook,
    LastFm,
    MetalArchives,
    MusicBrainz,
    RateYourMusic,
    Wikipedia
  )
  private val hostsByUrl = hosts.mapBy(_.url)
  private val hostsByName = hosts.mapBy(_.name.toLowerCase)

  def fromUrl(url: Url): Option[Host] = hostsByUrl get url.host
  def defaultFor(url: Url): Host = {
    Host(name = url.address
        .toLowerCase
        .replaceAll("^https?://", "")
        .replaceAll("^www\\.", "")
        .takeWhile(_ != '.'), url = url.host)
  }
}

