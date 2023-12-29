package backend.external

import io.lemonlabs.uri.Url
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class HostTest extends FreeSpec with AuxSpecs {
  "fromUrl" - {
    "existing" - {
      "MetalArchives" in {
        Host
          .withUrl(Url.parse("http://www.metal-archives.com/bands/Cruachan/86"))
          .get shouldReturn Host.MetalArchives
      }
      "Last.fm" in {
        Host.withUrl(Url.parse("http://www.last.fm/music/Deafheaven)")).get shouldReturn Host.LastFm
      }
    }
    "non-existing" in {
      Host.withUrl(Url.parse("https://twitter.com/springsteen")) shouldReturn None
    }
  }
}
