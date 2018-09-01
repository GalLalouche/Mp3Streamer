package backend.external.expansions

import backend.Url
import backend.recon.{Album, Artist}
import com.google.inject.Provides
import common.io.InternetTalker

class WikipediaAlbumFinderTest extends SameHostExpanderSpec {
  @Provides private def createExpander(it: InternetTalker): SameHostExpander = new WikipediaAlbumFinder(it)
  private val DiscographyExtractLink = "https://en.wikipedia.org/w/index.php?title=Lady_in_Gold_(album)&redirect=no"
  private val Redirected = "wiki_redirected.html"
  private val NotRedirected = "wikipedia-discography.html"
  "extract the album when possible" in {
    findAlbum("wikipedia-discography.html", Album("Lady in Gold", 2016, Artist("Blues Pills")),
      DiscographyExtractLink -> NotRedirected)
        .get.link shouldReturn Url("https://en.wikipedia.org/wiki/Lady_in_Gold_(album)")
  }
  "Return nothing on red link" in {
    findAlbum("wiki_redlink.html", Album("Graveward", 2015, Artist("Sigh"))) shouldReturn None
  }
  "Return nothing on redirected link" in {
    findAlbum("wikipedia-discography.html", Album("Lady in Gold", 2016, Artist("Blues Pills")),
      DiscographyExtractLink -> Redirected) shouldReturn None
  }
  "Tries all links until one of them works" in {
    // The below HTML includes two links, the first one is redirected, and the second is disambiguated.
    findAlbum("two_wiki_links.html", Album("Preachers of the night", 2013, Artist("Powerwolf")),
      "https://en.wikipedia.org/w/index.php?title=Preachers_of_the_Night_(album)&redirect=no" -> NotRedirected,
      "https://en.wikipedia.org/w/index.php?title=Preachers_of_the_Night&redirect=no" -> Redirected)
        .get.link shouldReturn Url("https://en.wikipedia.org/wiki/Preachers_of_the_Night_(album)")
  }
}
