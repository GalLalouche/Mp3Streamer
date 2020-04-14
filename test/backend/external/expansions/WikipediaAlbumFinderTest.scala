package backend.external.expansions

import backend.Url
import backend.recon.{Album, Artist}
import net.codingwell.scalaguice.ScalaModule

class WikipediaAlbumFinderTest extends SameHostExpanderSpec {
  override protected def module = new ScalaModule {
    override def configure() = {
      bind[SameHostExpander].to[WikipediaAlbumFinder]
    }
  }
  private val DiscographyExtractLink =
    "https://en.wikipedia.org/w/index.php?title=Lady_in_Gold_(album)&redirect=no"
  private val HebrewDiscographyExtractLink =
    "https://he.wikipedia.org/w/index.php?title=%D7%91%D7%A9%D7%93%D7%95%D7%AA&redirect=no"
  private val Redirected = "wiki_redirected.html"
  private val NotRedirected = "wikipedia-discography.html"

  "extract the album when possible" in {
    findAlbum("wikipedia-discography.html", Album("Lady in Gold", 2016, Artist("Blues Pills")),
      DiscographyExtractLink -> NotRedirected)
        .mapValue(_.link shouldReturn Url("https://en.wikipedia.org/wiki/Lady_in_Gold_(album)"))
  }
  "Choose correct language" in {
    findAlbum("wikipedia-discography-hebrew.html", Album("בשדות", 2006, Artist("גבריאל בלחסן")),
      HebrewDiscographyExtractLink -> NotRedirected)
        .mapValue(_.link shouldReturn Url("https://he.wikipedia.org/wiki/%D7%91%D7%A9%D7%93%D7%95%D7%AA"))
  }
  "Return nothing on red link" in {
    findAlbum("wiki_redlink.html", Album("Graveward", 2015, Artist("Sigh"))).shouldEventuallyReturnNone()
  }
  "Return nothing on redirected link" in {
    findAlbum("wikipedia-discography.html", Album("Lady in Gold", 2016, Artist("Blues Pills")),
      DiscographyExtractLink -> Redirected).shouldEventuallyReturnNone()
  }
  "Tries all links until one of them works" in {
    // The below HTML includes two links, the first one is redirected, and the second is disambiguated.
    findAlbum("two_wiki_links.html", Album("Preachers of the night", 2013, Artist("Powerwolf")),
      "https://en.wikipedia.org/w/index.php?title=Preachers_of_the_Night_(album)&redirect=no" -> NotRedirected,
      "https://en.wikipedia.org/w/index.php?title=Preachers_of_the_Night&redirect=no" -> Redirected)
        .mapValue(_.link shouldReturn Url("https://en.wikipedia.org/wiki/Preachers_of_the_Night_(album)"))
  }
  "Links starting with // are ignored" in {
    findAlbum("wiki_double_slash_external.html", Album("Alvvays", 2014, Artist("Alvvays")),
      "https://en.wikipedia.org/w/index.php?title=Alvvays_(album)&redirect=no" -> NotRedirected)
        .mapValue(_.link shouldReturn Url("https://en.wikipedia.org/wiki/Alvvays_(album)"))
  }

  "Ignores external links" in {
    findAlbum("wikipedia_http_prefix.html", Album("Tick Tock", 2009, Artist("Gazpacho")))
        .shouldEventuallyReturnNone()
  }
}
