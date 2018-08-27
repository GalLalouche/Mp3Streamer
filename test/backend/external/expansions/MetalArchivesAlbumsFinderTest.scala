package backend.external.expansions

import backend.Url
import backend.configs.Configuration
import backend.recon.{Album, Artist}
import net.codingwell.scalaguice.InjectorExtensions._

class MetalArchivesAlbumsFinderTest extends SameHostExpanderSpec {
  override private[expansions] def createExpander(implicit c: Configuration) =
    c.injector.instance[MetalArchivesAlbumsFinder]
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
