package backend.external

import backend.Url
import backend.configs.TestConfiguration
import backend.recon.{Album, Artist, ReconID}
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.scalatest.FreeSpec

class MbHtmlLinkExtractorTest extends FreeSpec with AuxSpecs {
  private def withDocument(name: String) =
    TestConfiguration().copy(_documentDownloader = _ => Jsoup.parse(getResourceFile(name + ".html").readAll))

  "parse artist links" in {
    implicit val c = withDocument("artist")
    val $ = new ArtistLinkExtractor
    val expected = Set(
      ExternalLink[Artist](Url("http://deafheaven.com/"), Host("home", Url("deafheaven.com"))),
      ExternalLink[Artist](Url("http://www.allmusic.com/artist/mn0002658855"), Host.AllMusic),
      ExternalLink[Artist](Url("https://deafheavens.bandcamp.com/"), Host("bandcamp", Url("deafheavens.bandcamp.com"))),
      ExternalLink[Artist](Url("http://www.discogs.com/artist/2025280"), Host("discogs", Url("www.discogs.com"))),
      ExternalLink[Artist](Url("http://www.last.fm/music/Deafheaven"), Host.LastFm),
      ExternalLink[Artist](Url("https://myspace.com/deafheaven"), Host("myspace", Url("myspace.com"))),
      ExternalLink[Artist](Url("https://rateyourmusic.com/artist/deafheaven"), Host("RateYourMusic", Url("rateyourmusic.com"))),
      ExternalLink[Artist](Url("http://www.metal-archives.com/bands/Deafheaven/3540315870"), Host("MetalArchives", Url("www.metal-archives.com"))),
      ExternalLink[Artist](Url("https://twitter.com/deafheavenband"), Host("twitter", Url("twitter.com"))),
      ExternalLink[Artist](Url("https://www.facebook.com/deafheaven"), Host("Facebook", Url("www.facebook.com"))),
      ExternalLink[Artist](Url("https://www.wikidata.org/wiki/Q5245804"), Host("wikidata", Url("www.wikidata.org"))),
      ExternalLink[Artist](Url("https://en.wikipedia.org/wiki/Deafheaven"), Host.Wikipedia),
      ExternalLink[Artist](Url("https://itunes.apple.com/es/album/id1123970968"), Host("itunes", Url("itunes.apple.com"))),
      ExternalLink[Artist](Url("https://musicbrainz.org/artist/foobar"), Host.MusicBrainz)
    )

    $(ReconID("foobar")).get.toSet shouldReturn expected
  }

  "parse album links" in {
    implicit val c = withDocument("album")
    val $ = new AlbumLinkExtractor
    val expected = Set(
      ExternalLink[Album](Url("http://www.discogs.com/master/559132"), Host("discogs", Url("www.discogs.com"))),
      ExternalLink[Album](Url("https://rateyourmusic.com/release/album/deafheaven/sunbather/"), Host("RateYourMusic", Url("rateyourmusic.com"))),
      ExternalLink[Album](Url("https://www.wikidata.org/wiki/Q15717528"), Host("wikidata", Url("www.wikidata.org"))),
      ExternalLink[Album](Url("https://en.wikipedia.org/wiki/Sunbather_(album)"), Host.Wikipedia),
      ExternalLink[Album](Url("https://musicbrainz.org/release-group/foobar"), Host.MusicBrainz)
    )

    $.apply(ReconID("foobar")).get shouldSetEqual expected
  }
}
