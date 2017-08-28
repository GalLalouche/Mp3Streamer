package backend.external

import backend.Url
import backend.configs.TestConfiguration
import backend.recon.{Album, Artist, ReconID}
import common.rich.RichFuture._
import common.rich.RichT._
import org.scalatest.FreeSpec

class MbHtmlLinkExtractorTest extends FreeSpec with DocumentSpecs {
  private def withDocument(name: String) =
    TestConfiguration().copy(_urlToBytesMapper = PartialFunction(getBytes(name + ".html").const))

  "parse artist links" in {
    implicit val c = withDocument("artist")
    val $ = new ArtistLinkExtractor
    val expected = Set(
      BaseLink[Artist](Url("http://deafheaven.com/"), Host("home", Url("deafheaven.com"))),
      BaseLink[Artist](Url("http://www.allmusic.com/artist/mn0002658855"), Host.AllMusic),
      BaseLink[Artist](Url("https://deafheavens.bandcamp.com/"), Host("bandcamp", Url("deafheavens.bandcamp.com"))),
      BaseLink[Artist](Url("http://www.discogs.com/artist/2025280"), Host("discogs", Url("www.discogs.com"))),
      BaseLink[Artist](Url("http://www.last.fm/music/Deafheaven"), Host.LastFm),
      BaseLink[Artist](Url("https://myspace.com/deafheaven"), Host("myspace", Url("myspace.com"))),
      BaseLink[Artist](Url("https://rateyourmusic.com/artist/deafheaven"), Host("RateYourMusic", Url("rateyourmusic.com"))),
      BaseLink[Artist](Url("http://www.metal-archives.com/bands/Deafheaven/3540315870"), Host("MetalArchives", Url("www.metal-archives.com"))),
      BaseLink[Artist](Url("https://twitter.com/deafheavenband"), Host("twitter", Url("twitter.com"))),
      BaseLink[Artist](Url("https://www.facebook.com/deafheaven"), Host("Facebook", Url("www.facebook.com"))),
      BaseLink[Artist](Url("https://www.wikidata.org/wiki/Q5245804"), Host("wikidata", Url("www.wikidata.org"))),
      BaseLink[Artist](Url("https://en.wikipedia.org/wiki/Deafheaven"), Host.Wikipedia),
      BaseLink[Artist](Url("https://itunes.apple.com/es/album/id1123970968"), Host("itunes", Url("itunes.apple.com"))),
      BaseLink[Artist](Url("https://musicbrainz.org/artist/foobar"), Host.MusicBrainz)
    )

    $(ReconID("foobar")).get.toSet shouldReturn expected
  }

  "parse album links" in {
    implicit val c = withDocument("album")
    val $ = new AlbumLinkExtractor
    val expected = Set(
      BaseLink[Album](Url("http://www.discogs.com/master/559132"), Host("discogs", Url("www.discogs.com"))),
      BaseLink[Album](Url("https://rateyourmusic.com/release/album/deafheaven/sunbather/"), Host("RateYourMusic", Url("rateyourmusic.com"))),
      BaseLink[Album](Url("https://www.wikidata.org/wiki/Q15717528"), Host("wikidata", Url("www.wikidata.org"))),
      BaseLink[Album](Url("https://en.wikipedia.org/wiki/Sunbather_(album)"), Host.Wikipedia),
      BaseLink[Album](Url("https://musicbrainz.org/release-group/foobar"), Host.MusicBrainz)
    )

    $.apply(ReconID("foobar")).get shouldSetEqual expected
  }
}
