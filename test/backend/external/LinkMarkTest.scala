package backend.external

import common.test.AuxSpecs
import org.scalatest.freespec.AnyFreeSpec

class LinkMarkTest extends AnyFreeSpec with AuxSpecs {
  "Text" - {
    "Read" in {
      val $ = LinkMark.Text("foobar")
      LinkMark.Text.read($.toString) shouldReturn $
    }
  }
}
