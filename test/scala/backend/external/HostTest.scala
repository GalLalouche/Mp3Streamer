package backend.external

import backend.Url
import common.AuxSpecs
import org.scalatest.FreeSpec

class HostTest extends FreeSpec with AuxSpecs {
  "fromUrl" - {
    "existing" in {
      Host.fromUrl(Url("http://www.metal-archives.com/bands/Cruachan/86")) shouldReturn Host.MetalArchives
    }
    "non-existing" - {
      "with http" in {
        Host.fromUrl(Url("https://www.discogs.com/artist/219986")) shouldReturn Host("discogs", Url("www.discogs.com"))
      }
      "without http" in {
        Host.fromUrl(Url("www.discogs.com/artist/219986")) shouldReturn Host("discogs", Url("www.discogs.com"))
      }
      "without www" in {
        Host.fromUrl(Url("https://twitter.com/springsteen")) shouldReturn Host("twitter", Url("twitter.com"))
      }
      "without www or http" in {
        Host.fromUrl(Url("http://rateyourmusic.com/artist/bruce_springsteen")) shouldReturn Host("rateyourmusic", Url("rateyourmusic.com"))
      }
    }
  }
}
