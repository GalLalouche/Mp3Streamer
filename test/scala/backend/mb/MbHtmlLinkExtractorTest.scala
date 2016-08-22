package backend.mb

import backend.Url
import backend.external.{ExternalLink, Host}
import backend.mb.{AlbumLinkExtractor, ArtistLinkExtractor}
import backend.recon.{Album, Artist, ReconID}
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.FreeSpec

import scala.concurrent.Future

class MbHtmlLinkExtractorTest extends FreeSpec with AuxSpecs {

  import backend.TestConfiguration._
  private def getDocument(name: String) = Future successful Jsoup.parse(getResourceFile(name + ".html").readAll)

  "parse artist links" in {
    val $ = new ArtistLinkExtractor() {
      override private[mb] def getHtml(artistId: ReconID): Future[Document] = getDocument("artist")
    }
    val expected = Set(
      ExternalLink[Artist](Url("http://deafheaven.com/"), Host("home", Url("deafheaven.com"))),
      ExternalLink[Artist](Url("http://www.allmusic.com/artist/mn0002658855"), Host("allmusic", Url("www.allmusic.com"))),
      ExternalLink[Artist](Url("https://deafheavens.bandcamp.com/"), Host("bandcamp", Url("deafheavens.bandcamp.com"))),
      ExternalLink[Artist](Url("http://www.discogs.com/artist/2025280"), Host("discogs", Url("www.discogs.com"))),
      ExternalLink[Artist](Url("http://www.last.fm/music/Deafheaven"), Host("lastfm", Url("www.last.fm"))),
      ExternalLink[Artist](Url("https://myspace.com/deafheaven"), Host("myspace", Url("myspace.com"))),
      ExternalLink[Artist](Url("https://rateyourmusic.com/artist/deafheaven"), Host("rateyourmusic", Url("rateyourmusic.com"))),
      ExternalLink[Artist](Url("http://www.metal-archives.com/bands/Deafheaven/3540315870"), Host("metalarchives", Url("www.metal-archives.com"))),
      ExternalLink[Artist](Url("https://twitter.com/deafheavenband"), Host("twitter", Url("twitter.com"))),
      ExternalLink[Artist](Url("https://www.facebook.com/deafheaven"), Host("facebook", Url("www.facebook.com"))),
      ExternalLink[Artist](Url("https://www.wikidata.org/wiki/Q5245804"), Host("wikidata", Url("www.wikidata.org"))),
      ExternalLink[Artist](Url("https://en.wikipedia.org/wiki/Deafheaven"), Host("wikipedia", Url("en.wikipedia.org"))),
      ExternalLink[Artist](Url("https://musicbrainz.org/artist/foobar"), Host("musicbrainz", Url("musicbrainz.org")))
    )

    $.apply(ReconID("foobar")).get.toSet shouldReturn expected
  }

  "parse album links" in {
    val $ = new AlbumLinkExtractor() {
      override private[mb] def getHtml(artistId: ReconID): Future[Document] = getDocument("album")
    }
    val expected = Set(
      ExternalLink[Album](Url("http://www.discogs.com/master/559132"), Host("discogs", Url("www.discogs.com"))),
      ExternalLink[Album](Url("https://rateyourmusic.com/release/album/deafheaven/sunbather/"), Host("rateyourmusic", Url("rateyourmusic.com"))),
      ExternalLink[Album](Url("https://www.wikidata.org/wiki/Q15717528"), Host("wikidata", Url("www.wikidata.org"))),
      ExternalLink[Album](Url("https://en.wikipedia.org/wiki/Sunbather_(album)"), Host("wikipedia", Url("en.wikipedia.org"))),
      ExternalLink[Album](Url("https://musicbrainz.org/release-group/foobar"), Host("musicbrainz", Url("musicbrainz.org")))
    )

    $.apply(ReconID("foobar")).get.toSet shouldReturn expected
  }
}
