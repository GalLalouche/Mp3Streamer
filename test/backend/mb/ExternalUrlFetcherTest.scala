package backend.mb

import backend.external.{BaseLink, DocumentSpecs, ExternalLinkProvider, Host}
import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist, ReconID}
import com.google.inject.Key
import io.lemonlabs.uri.Url
import org.scalatest.freespec.AsyncFreeSpec

import common.rich.RichT._

class ExternalUrlFetcherTest extends AsyncFreeSpec with DocumentSpecs {
  private def withDocument(name: String) =
    TestModuleConfiguration(_urlToBytesMapper =
      getBytes(name + "_rel-urls.json").partialConst,
    ).injector

  "parse artist links" in {
    val injector = withDocument("artist")
    // Instance with type aliases is bugged it seems
    val $ = injector.getInstance(new Key[ExternalLinkProvider[Artist]]() {})
    val expected = Vector(
      BaseLink[Artist](Url.parse("https://www.allmusic.com/artist/mn0002658855"), Host.AllMusic),
      BaseLink[Artist](Url.parse("https://www.last.fm/music/Deafheaven"), Host.LastFm),
      BaseLink[Artist](
        Url.parse("https://rateyourmusic.com/artist/deafheaven"),
        Host("RateYourMusic", Url.parse("rateyourmusic.com")),
      ),
      BaseLink[Artist](
        Url.parse("https://www.metal-archives.com/bands/Deafheaven/3540315870"),
        Host("MetalArchives", Url.parse("www.metal-archives.com")),
      ),
      BaseLink[Artist](
        Url.parse("https://www.facebook.com/deafheaven"),
        Host("Facebook", Url.parse("www.facebook.com")),
      ),
      BaseLink[Artist](Url.parse("https://www.wikidata.org/wiki/Q5245804"), Host.Wikidata),
      BaseLink[Artist](Url.parse("https://musicbrainz.org/artist/foobar"), Host.MusicBrainz),
      BaseLink[Artist](Url.parse("https://deafheavens.bandcamp.com/"), Host.Bandcamp),
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
      BaseLink[Album](Url.parse("https://musicbrainz.org/release-group/foobar"), Host.MusicBrainz),
      BaseLink[Album](Url.parse("https://www.allmusic.com/album/mw0002537857"), Host.AllMusic),
    )

    $(ReconID("foobar")).map(_ shouldMultiSetEqual expected)
  }
}
