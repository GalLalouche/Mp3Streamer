package common.io

import org.scalatest.freespec.AnyFreeSpec

import common.test.AuxSpecs

class UtfDecoderTest extends AnyFreeSpec with AuxSpecs {
  "apply" in {
    UtfDecoder("http://www.progarchives.com/album.asp?id\\u003d53534") shouldReturn
      "http://www.progarchives.com/album.asp?id=53534"
  }
}
