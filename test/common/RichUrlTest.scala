package common

import org.scalatest.FreeSpec

import _root_.io.lemonlabs.uri.Url
import common.test.AuxSpecs
import common.RichUrl.richUrl

class RichUrlTest extends FreeSpec with AuxSpecs {
  "host" - {
    "correct" in {
      Url.parse("http://www.metal-archives.com/bands/Cruachan/86/").hostUrl shouldReturn Url.parse(
        "www.metal-archives.com",
      )
    }
    "without http" in {
      Url.parse("www.metal-archives.com/bands/Cruachan/86/").hostUrl shouldReturn Url.parse(
        "www.metal-archives.com",
      )
    }
    "for bandcamp" in {
      Url.parse("https://shanipeleg1.bandcamp.com/album/-").hostUrl shouldReturn Url.parse(
        "bandcamp.com",
      )
    }
    "no www" in {
      Url.parse("https://bandcamp.com").hostUrl shouldReturn Url.parse("bandcamp.com")
    }
  }

  "addPathPartRaw" - {
    "should add / if needed" in {
      Url.parse("www.faceboom.com").addPathPartRaw("foobar") shouldReturn Url.parse(
        "www.faceboom.com/foobar",
      )
    }
    "should not add / when not needed" - {
      "this ends in /" in {
        Url.parse("www.faceboom.com/").addPathPartRaw("foobar") shouldReturn Url.parse(
          "www.faceboom.com/foobar",
        )
      }
      "that starts with /" in {
        Url.parse("www.faceboom.com").addPathPartRaw("/foobar") shouldReturn Url.parse(
          "www.faceboom.com/foobar",
        )
      }
      "both" in {
        Url.parse("www.faceboom.com/").addPathPartRaw("/foobar") shouldReturn Url.parse(
          "www.faceboom.com/foobar",
        )
      }
    }
  }
}
