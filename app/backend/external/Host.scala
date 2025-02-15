package backend.external

import enumeratum.{Enum, EnumEntry}
import io.lemonlabs.uri.Url

import common.RichUrl.richUrl
import common.rich.collections.RichTraversableOnce._

sealed case class Host private (name: String, url: Url) extends EnumEntry
object Host extends Enum[Host] {
  // Can't be case objects since they extend a case class.
  object AllMusic extends Host("AllMusic", Url.parse("www.allmusic.com"))
  object Bandcamp extends Host("Bandcamp", Url.parse("bandcamp.com"))
  object Facebook extends Host("Facebook", Url.parse("www.facebook.com"))
  object LastFm extends Host("LastFm", Url.parse("www.last.fm"))
  object MetalArchives extends Host("MetalArchives", Url.parse("www.metal-archives.com"))
  object MusicBrainz extends Host("MusicBrainz", Url.parse("musicbrainz.org"))
  object RateYourMusic extends Host("RateYourMusic", Url.parse("rateyourmusic.com"))
  object Wikipedia extends Host("Wikipedia", Url.parse("wikipedia.org"))
  object Wikidata extends Host("Wikidata", Url.parse("www.wikidata.org"))
  override val values = findValues

  def withUrl(url: Url): Option[Host] = hostsByUrl.get(url.hostUrl)
  private val hostsByUrl = values.mapBy(_.url)
}
