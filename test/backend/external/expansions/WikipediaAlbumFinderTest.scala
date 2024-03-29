package backend.external.expansions

import backend.recon.{Album, Artist}
import io.lemonlabs.uri.Url
import net.codingwell.scalaguice.ScalaModule

import common.rich.func.BetterFutureInstances._

class WikipediaAlbumFinderTest extends SameHostExpanderSpec {
  protected override def module = new ScalaModule {
    override def configure() =
      bind[SameHostExpander].to[WikipediaAlbumFinder]
  }
  private val DiscographyExtractLink =
    "https://en.wikipedia.org/w/index.php?title=Lady_in_Gold_(album)&redirect=no"
  private val HebrewDiscographyExtractLink =
    "https://he.wikipedia.org/w/index.php?title=%D7%91%D7%A9%D7%93%D7%95%D7%AA&redirect=no"
  private val Redirected = "wiki_redirected.html"
  private val NotRedirected = "wikipedia-discography.html"

  "extract the album when possible" in {
    findAlbum(
      "wikipedia-discography.html",
      Album("Lady in Gold", 2016, Artist("Blues Pills")),
      DiscographyExtractLink -> NotRedirected,
    )
      .mapValue(_.link shouldReturn Url.parse("https://en.wikipedia.org/wiki/Lady_in_Gold_(album)"))
  }
  "Return the correct link to a wikipedia site, not a file." in {
    findAlbum(
      "wikipedia-with-file.html",
      Album("Fear of a Blank Planet", 2007, Artist("Porcupine tree")),
      "https://en.wikipedia.org/w/index.php?title=Fear_of_a_Blank_Planet&redirect=no" -> NotRedirected,
      "https://en.wikipedia.org/w/index.php?title=Fear_of_a_Blank_Planet_(song)&redirect=no" -> Redirected,
    ).map(_.link)
      .valueShouldEventuallyReturn(
        Url.parse("https://en.wikipedia.org/wiki/Fear_of_a_Blank_Planet"),
      )
  }
  "Choose correct language" in {
    findAlbum(
      "wikipedia-discography-hebrew.html",
      Album("בשדות", 2006, Artist("גבריאל בלחסן")),
      HebrewDiscographyExtractLink -> NotRedirected,
    )
      .mapValue(
        _.link shouldReturn Url.parse(
          "https://he.wikipedia.org/wiki/%D7%91%D7%A9%D7%93%D7%95%D7%AA",
        ),
      )
  }
  "Return nothing on red link" in {
    findAlbum("wiki_redlink.html", Album("Graveward", 2015, Artist("Sigh")))
      .shouldEventuallyReturnNone()
  }
  "Return nothing on redirected link" in {
    findAlbum(
      "wikipedia-discography.html",
      Album("Lady in Gold", 2016, Artist("Blues Pills")),
      DiscographyExtractLink -> Redirected,
    ).shouldEventuallyReturnNone()
  }
  "Tries all links until one of them works" in {
    // The below HTML includes two links, the first one is redirected, and the second is disambiguated.
    findAlbum(
      "two_wiki_links.html",
      Album("Preachers of the night", 2013, Artist("Powerwolf")),
      "https://en.wikipedia.org/w/index.php?title=Preachers_of_the_Night_(album)&redirect=no" -> NotRedirected,
      "https://en.wikipedia.org/w/index.php?title=Preachers_of_the_Night&redirect=no" -> Redirected,
    )
      .mapValue(
        _.link shouldReturn Url.parse(
          "https://en.wikipedia.org/wiki/Preachers_of_the_Night_(album)",
        ),
      )
  }
  "Links starting with // are ignored" in {
    findAlbum(
      "wiki_double_slash_external.html",
      Album("Alvvays", 2014, Artist("Alvvays")),
      "https://en.wikipedia.org/w/index.php?title=Alvvays_(album)&redirect=no" -> NotRedirected,
    )
      .mapValue(_.link shouldReturn Url.parse("https://en.wikipedia.org/wiki/Alvvays_(album)"))
  }

  "Ignores external links" in {
    findAlbum("wikipedia_http_prefix.html", Album("Tick Tock", 2009, Artist("Gazpacho")))
      .shouldEventuallyReturnNone()
  }
}
