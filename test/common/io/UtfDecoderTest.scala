package common.io

import common.test.AuxSpecs
import org.scalatest.FreeSpec

class UtfDecoderTest extends FreeSpec with AuxSpecs {
  "apply" in {
    UtfDecoder("http://www.progarchives.com/album.asp?id\\u003d53534") shouldReturn
      "http://www.progarchives.com/album.asp?id=53534"
  }
}
