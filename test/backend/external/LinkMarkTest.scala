package backend.external

import org.scalatest.freespec.AnyFreeSpec

import common.test.AuxSpecs

class LinkMarkTest extends AnyFreeSpec with AuxSpecs {
  "Text" - {
    "Read" in {
      val $ = LinkMark.Text("foobar")
      LinkMark.Text.read($.toString) shouldReturn $
    }
  }
}
