package backend.external

import backend.recon.{Album, Artist}
import backend.{TestConfiguration, Url}
import org.scalatest.FreeSpec

class MetalArchivesAlbumsFinderTest extends FreeSpec with DocumentSpecs {
  private implicit val c = TestConfiguration
  private val $ = new MetalArchivesAlbumsFinder()
  private def aux(a: Album) = {
    $.aux(getDocument("metal-archives-discography.html"), a)
  }
  "aux" - {
    "throws exception on wrong artist" in {
      an[IllegalArgumentException] should be thrownBy $.fromUrl(
        Url("http://www.metal-archives.com/bands/Cruachan/86"), Album("Let it Bleed", Artist("Rolling Stones")))
    }
    "return none when there is no matching album" in {
      aux(Album("Let it Bleed", Artist("Cruachan"))) shouldBe 'empty
    }
    "find album" in {
      aux(Album("Blood for the Blood God", Artist("Cruachan"))).get shouldReturn
          Url("http://www.metal-archives.com/albums/Cruachan/Blood_for_the_Blood_God/475926")
    }
  }
}
