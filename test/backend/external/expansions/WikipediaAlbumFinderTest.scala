package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.recon.{Album, Artist}

class WikipediaAlbumFinderTest extends SameHostExpanderSpec {
  override private[expansions] def createExpander(implicit c: TestConfiguration) =
    new WikipediaAlbumFinder

  "extract the album when possible" in {
    findAlbum("wikipedia-discography.html", Album("Lady in Gold", 2016, Artist("Blues Pills")))
        .get.link shouldReturn Url("https://en.wikipedia.org/wiki/Lady_in_Gold_(album)")
  }
  "Return nothing on red link" in {
    findAlbum("wiki_redlink.html", Album("Graveward", 2015, Artist("Sigh"))) shouldReturn None
  }
}
