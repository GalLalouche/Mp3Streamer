package backend.external

import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist, ReconID}
import com.google.inject.Key
import common.rich.RichT._
import io.lemonlabs.uri.Url
import org.scalatest.AsyncFreeSpec

class MbHtmlLinkExtractorTest extends AsyncFreeSpec with DocumentSpecs {
  private def withDocument(name: String) =
    TestModuleConfiguration(_urlToBytesMapper = getBytes(name + ".html").partialConst).injector

  "parse artist links" in {
    val injector = withDocument("artist")
    // Instance with type aliases is bugged it seems
    val $ = injector.getInstance(new Key[ExternalLinkProvider[Artist]]() {})
    val expected = Vector(
      BaseLink[Artist](Url.parse("http://www.allmusic.com/artist/mn0002658855"), Host.AllMusic),
      BaseLink[Artist](Url.parse("http://www.last.fm/music/Deafheaven"), Host.LastFm),
      BaseLink[Artist](
        Url.parse("https://rateyourmusic.com/artist/deafheaven"),
        Host("RateYourMusic", Url.parse("rateyourmusic.com")),
      ),
      BaseLink[Artist](
        Url.parse("http://www.metal-archives.com/bands/Deafheaven/3540315870"),
        Host("MetalArchives", Url.parse("www.metal-archives.com")),
      ),
      BaseLink[Artist](
        Url.parse("https://www.facebook.com/deafheaven"),
        Host("Facebook", Url.parse("www.facebook.com")),
      ),
      BaseLink[Artist](Url.parse("https://www.wikidata.org/wiki/Q5245804"), Host.Wikidata),
      BaseLink[Artist](Url.parse("https://en.wikipedia.org/wiki/Deafheaven"), Host.Wikipedia),
      BaseLink[Artist](Url.parse("https://musicbrainz.org/artist/foobar"), Host.MusicBrainz),
      BaseLink[Artist](Url.parse("https://deafheavens.bandcamp.com/"), Host.Bandcamp),
      /* Ignored */
      // BaseLink[Artist](Url.parse("http://deafheaven.com/"), Host("home", Url.parse("deafheaven.com"))),
      // BaseLink[Artist](Url.parse("https://itunes.apple.com/es/album/id1123970968"), Host("itunes", Url.parse("itunes.apple.com"))),
      // BaseLink[Artist](Url.parse("http://www.discogs.com/artist/2025280"), Host("discogs", Url.parse("www.discogs.com"))),
      // BaseLink[Artist](Url.parse("https://myspace.com/deafheaven"), Host("myspace", Url.parse("myspace.com"))),
      // BaseLink[Artist](Url.parse("https://twitter.com/deafheavenband"), Host("twitter", Url.parse("twitter.com"))),
    )

    $(ReconID("foobar")).map(_ shouldMultiSetEqual expected)
  }

  "parse album links" in {
    val injector = withDocument("album")
    val $ = injector.getInstance(new Key[ExternalLinkProvider[Album]]() {})
    val expected = Vector(
      BaseLink[Album](
        Url.parse("https://rateyourmusic.com/release/album/deafheaven/sunbather/"),
        Host("RateYourMusic", Url.parse("rateyourmusic.com")),
      ),
      BaseLink[Album](Url.parse("https://www.wikidata.org/wiki/Q15717528"), Host.Wikidata),
      BaseLink[Album](Url.parse("https://en.wikipedia.org/wiki/Sunbather_(album)"), Host.Wikipedia),
      BaseLink[Album](Url.parse("https://musicbrainz.org/release-group/foobar"), Host.MusicBrainz),
      /* Ignored */
      // BaseLink[Album](Url.parse("http://www.discogs.com/master/559132"), Host("discogs", Url.parse("www.discogs.com"))),
    )

    $(ReconID("foobar")).map(_ shouldMultiSetEqual expected)
  }
}
