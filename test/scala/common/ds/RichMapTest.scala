package common.ds
import common.AuxSpecs
import org.scalatest.FreeSpec

class RichMapTest extends FreeSpec with AuxSpecs {
  import common.ds.RichMap._
  "updateWith" - {
    "modify" in {
      Map("foo" -> 2).modified("foo", _ + 1) shouldReturn Map("foo" -> 3)
    }
  }
}
