package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.external.DocumentSpecs
import backend.recon.{Album, Artist}
import org.scalatest.FreeSpec

class MetalArchivesAlbumsFinderTest extends FreeSpec with DocumentSpecs {
  private implicit val c = TestConfiguration()
  private val $ = new MetalArchivesAlbumsFinder()
  private def aux(a: Album) = {
    $.aux(getDocument("metal-archives-discography.html"), a)
  }
  "aux" - {
    "return none when there is no matching album" in {
      aux(Album("Let it Bleed", 1928, Artist("Cruachan"))) shouldBe 'empty
    }
    "find album" in {
      aux(Album("Blood for the Blood God", 2014, Artist("Cruachan"))).get shouldReturn
          Url("http://www.metal-archives.com/albums/Cruachan/Blood_for_the_Blood_God/475926")
    }
  }
}