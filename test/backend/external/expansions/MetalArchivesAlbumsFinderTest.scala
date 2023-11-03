package backend.external.expansions

import backend.recon.{Album, Artist}
import backend.Url
import net.codingwell.scalaguice.ScalaModule

class MetalArchivesAlbumsFinderTest extends SameHostExpanderSpec {
  protected override def module = new ScalaModule {
    override def configure() =
      bind[SameHostExpander].to[MetalArchivesAlbumsFinder]
  }
  protected override val artistUrl = "https://www.metal-archives.com/bands/Cruachan/86"
  protected override val expandingUrl =
    "http://www.metal-archives.com/band/discography/id/86/tab/all"

  "return none when there is no matching album" in {
    findAlbum("metal-archives-discography.html", Album("Let it Bleed", 1928, Artist("Cruachan")))
      .shouldEventuallyReturnNone()
  }
  "find album" in {
    findAlbum(
      "metal-archives-discography.html",
      Album("Blood for the Blood God", 2014, Artist("Cruachan")),
    )
      .mapValue(
        _.link shouldReturn Url(
          "http://www.metal-archives.com/albums/Cruachan/Blood_for_the_Blood_God/475926",
        ),
      )
  }
}
