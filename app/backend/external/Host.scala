package backend.external

import java.util.regex.Pattern

import backend.Url
import com.google.common.annotations.VisibleForTesting

import monocle.macros.Lenses

import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichString._

// TODO make an Enum
@Lenses
case class Host(name: String, url: Url) {
  import Host._

  // TODO Host is being abused by representing two different things. Extract this behavior to a class.
  def canonical: Host = hostsByName.getOrElse(name.toLowerCase.removeAll(Suffixes), defaultFor(url))
}

object Host {
  private val Suffixes = Pattern compile "[*?]$"
  object AllMusic extends Host("AllMusic", Url("www.allmusic.com"))
  object Facebook extends Host("Facebook", Url("www.facebook.com"))
  object LastFm extends Host("LastFm", Url("www.last.fm"))
  object MetalArchives extends Host("MetalArchives", Url("www.metal-archives.com"))
  object MusicBrainz extends Host("MusicBrainz", Url("musicbrainz.org"))
  object RateYourMusic extends Host("RateYourMusic", Url("rateyourmusic.com"))
  object Wikipedia extends Host("Wikipedia", Url("en.wikipedia.org"))
  object Wikidata extends Host("Wikidata", Url("www.wikidata.org"))
  val hosts = Vector(
    AllMusic,
    Facebook,
    LastFm,
    MetalArchives,
    MusicBrainz,
    RateYourMusic,
    Wikipedia,
    Wikidata,
  )
  private val hostsByUrl = hosts.mapBy(_.url)
  private val hostsByName = hosts.mapBy(_.name.toLowerCase)

  def fromUrl(url: Url): Option[Host] = hostsByUrl get url.host
  @VisibleForTesting
  private[external] def defaultFor(url: Url): Host = Host(
    name = url.address
        .toLowerCase
        .replaceAll("^https?://", "")
        .replaceAll("^www\\.", "")
        .takeWhile(_ != '.'),
    url = url.host,
  )
}

