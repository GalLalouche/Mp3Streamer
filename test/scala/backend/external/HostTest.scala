package backend.external

import backend.Url
import common.AuxSpecs
import org.scalatest.FreeSpec

class HostTest extends FreeSpec with AuxSpecs {
  "fromUrl" - {
    "existing" - {
      "MetalArchives" in {
        Host.fromUrl(Url("http://www.metal-archives.com/bands/Cruachan/86")).get shouldReturn Host.MetalArchives
      }
      "Last.fm" in {
        Host.fromUrl(Url("http://www.last.fm/music/Deafheaven)")).get shouldReturn Host.LastFm
      }
    }
    "non-existing" - {
      "with http" in {
        Host.defaultFor(Url("https://www.discogs.com/artist/219986")) shouldReturn Host("discogs", Url("www.discogs.com"))
      }
      "without http" in {
        Host.defaultFor(Url("www.discogs.com/artist/219986")) shouldReturn Host("discogs", Url("www.discogs.com"))
      }
      "without www" in {
        Host.defaultFor(Url("https://twitter.com/springsteen")) shouldReturn Host("twitter", Url("twitter.com"))
      }
      "without www or http" in {
        Host.defaultFor(Url("http://rateyourmusic.com/artist/bruce_springsteen")) shouldReturn Host("rateyourmusic", Url("rateyourmusic.com"))
      }
    }
  }
  "canonize" - {
    "ending with *" in {
      Host("LastFm*", Url("www.last.fm")).canonize shouldReturn Host.LastFm
    }
  }
}
