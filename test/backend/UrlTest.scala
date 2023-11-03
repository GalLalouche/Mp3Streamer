package backend

import org.scalatest.FreeSpec

import common.test.AuxSpecs

class UrlTest extends FreeSpec with AuxSpecs {
  "ctor" - {
    "illegal" - {
      "empty" in {
        an[IllegalArgumentException] should be thrownBy Url("")
      }
      "whitespace" in {
        an[IllegalArgumentException] should be thrownBy Url("\n\t \r")
      }
    }
  }
  "host" - {
    "correct" in {
      Url("http://www.metal-archives.com/bands/Cruachan/86/").host shouldReturn Url(
        "www.metal-archives.com",
      )
    }
    "without http" in {
      Url("www.metal-archives.com/bands/Cruachan/86/").host shouldReturn Url(
        "www.metal-archives.com",
      )
    }
    "for bandcamp" in {
      Url("https://shanipeleg1.bandcamp.com/album/-").host shouldReturn Url("bandcamp.com")
    }
    "no www" in {
      Url("https://bandcamp.com").host shouldReturn Url("bandcamp.com")
    }
  }
  "+/" - {
    "should add / if needed" in {
      Url("www.faceboom.com") +/ "foobar" shouldReturn Url("www.faceboom.com/foobar")
    }
    "should not add / when not needed" - {
      "this ends in /" in {
        Url("www.faceboom.com/") +/ "foobar" shouldReturn Url("www.faceboom.com/foobar")
      }
      "that starts with /" in {
        Url("www.faceboom.com") +/ "/foobar" shouldReturn Url("www.faceboom.com/foobar")
      }
      "both" in {
        Url("www.faceboom.com/") +/ "/foobar" shouldReturn Url("www.faceboom.com/foobar")
      }
    }
  }
}
