package backend.external

import backend.Url
import enumeratum.{Enum, EnumEntry}

import common.rich.collections.RichTraversableOnce._

sealed case class Host private(name: String, url: Url) extends EnumEntry
object Host extends Enum[Host] {
  // TODO why aren't these case objects?
  object AllMusic extends Host("AllMusic", Url("www.allmusic.com"))
  object Bandcamp extends Host("Bandcamp", Url("bandcamp.com"))
  object Facebook extends Host("Facebook", Url("www.facebook.com"))
  object LastFm extends Host("LastFm", Url("www.last.fm"))
  object MetalArchives extends Host("MetalArchives", Url("www.metal-archives.com"))
  object MusicBrainz extends Host("MusicBrainz", Url("musicbrainz.org"))
  object RateYourMusic extends Host("RateYourMusic", Url("rateyourmusic.com"))
  object Wikipedia extends Host("Wikipedia", Url("wikipedia.org"))
  object Wikidata extends Host("Wikidata", Url("www.wikidata.org"))
  override val values = findValues

  def withUrl(url: Url): Option[Host] = hostsByUrl.get(url.host)
  private val hostsByUrl = values.mapBy(_.url)
}

