package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.recon.{Album, Artist}

class WikipediaAlbumFinderTest extends SameHostExpanderSpec {
  override private[expansions] def createExpander(implicit c: TestConfiguration) =
    new WikipediaAlbumFinder

  private val DiscographyExtractLink = "https://en.wikipedia.org/w/index.php?title=Lady_in_Gold_(album)&redirect=no"
  "extract the album when possible" in {
    findAlbum("wikipedia-discography.html", Album("Lady in Gold", 2016, Artist("Blues Pills")),
      DiscographyExtractLink -> "wikipedia-discography.html")
        .get.link shouldReturn Url("https://en.wikipedia.org/wiki/Lady_in_Gold_(album)")
  }
  "Return nothing on red link" in {
    findAlbum("wiki_redlink.html", Album("Graveward", 2015, Artist("Sigh"))) shouldReturn None
  }
  "Return nothing on redirected link" in {
    findAlbum("wikipedia-discography.html", Album("Lady in Gold", 2016, Artist("Blues Pills")),
      DiscographyExtractLink -> "wiki_redirected.html") shouldReturn None
  }
}
