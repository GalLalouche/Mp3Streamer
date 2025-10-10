package backend.external

import common.test.AuxSpecs
import org.scalatest.FreeSpec

class LinkMarkTest extends FreeSpec with AuxSpecs {
  "Text" - {
    "Read" in {
      val $ = LinkMark.Text("foobar")
      LinkMark.Text.read($.toString) shouldReturn $
    }
  }
}
