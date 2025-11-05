package common.io

import common.test.AuxSpecs
import org.scalatest.freespec.AnyFreeSpec

class UtfDecoderTest extends AnyFreeSpec with AuxSpecs {
  "apply" in {
    UtfDecoder("http://www.progarchives.com/album.asp?id\\u003d53534") shouldReturn
      "http://www.progarchives.com/album.asp?id=53534"
  }
}
