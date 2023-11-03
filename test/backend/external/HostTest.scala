package backend.external

import org.scalatest.FreeSpec

import backend.Url
import common.test.AuxSpecs

class HostTest extends FreeSpec with AuxSpecs {
  "fromUrl" - {
    "existing" - {
      "MetalArchives" in {
        Host
          .withUrl(Url("http://www.metal-archives.com/bands/Cruachan/86"))
          .get shouldReturn Host.MetalArchives
      }
      "Last.fm" in {
        Host.withUrl(Url("http://www.last.fm/music/Deafheaven)")).get shouldReturn Host.LastFm
      }
    }
    "non-existing" in {
      Host.withUrl(Url("https://twitter.com/springsteen")) shouldReturn None
    }
  }
}
