package backend.external.expansions

import backend.Url
import backend.recon.{Album, Artist}
import com.google.inject.Provides
import common.io.InternetTalker

class MetalArchivesAlbumsFinderTest extends SameHostExpanderSpec {
  @Provides private def createExpander(it: InternetTalker): SameHostExpander =
    new MetalArchivesAlbumsFinder(it)
  override protected val artistUrl = "https://www.metal-archives.com/bands/Cruachan/86"
  override protected val expandingUrl = "http://www.metal-archives.com/band/discography/id/86/tab/all"

  "return none when there is no matching album" in {
    findAlbum("metal-archives-discography.html", Album("Let it Bleed", 1928, Artist("Cruachan"))) shouldBe 'empty
  }
  "find album" in {
    findAlbum("metal-archives-discography.html", Album("Blood for the Blood God", 2014, Artist("Cruachan")))
        .get.link shouldReturn Url("http://www.metal-archives.com/albums/Cruachan/Blood_for_the_Blood_God/475926")
  }
}
