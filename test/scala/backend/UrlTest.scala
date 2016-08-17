package backend

import common.AuxSpecs
import org.scalatest.FreeSpec

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
      Url("http://www.metal-archives.com/bands/Cruachan/86/").host shouldReturn Url("www.metal-archives.com")
    }
  }
}
