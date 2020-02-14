package backend.external

import org.scalatest.FreeSpec

import common.test.AuxSpecs

class LinkMarkTest extends FreeSpec with AuxSpecs {
  "Text" - {
    "Read" in {
      val $ = LinkMark.Text("foobar")
      LinkMark.Text.read($.toString) shouldReturn $
    }
  }
}
