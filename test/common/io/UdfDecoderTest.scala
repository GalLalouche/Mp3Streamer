package common.io

import org.scalatest.FreeSpec

import common.test.AuxSpecs

class UdfDecoderTest extends FreeSpec with AuxSpecs {
  "apply" in {
    UtfDecoder("http://www.progarchives.com/album.asp?id\\u003d53534") shouldReturn
        "http://www.progarchives.com/album.asp?id=53534"
  }
}
