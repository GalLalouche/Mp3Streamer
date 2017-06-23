package common.ds

import common.AuxSpecs
import org.scalatest.FreeSpec

class RichMapTest extends FreeSpec with AuxSpecs {
  import common.ds.RichMap._
  "modify" in {
    Map("foo" -> 2).modified("foo", _ + 1) shouldReturn Map("foo" -> 3)
  }
  "updateWith" - {
    "has previous value" in {
      Map("foo" -> 2).updateWith("foo", 1, _ + _) shouldReturn Map("foo" -> 3)
    }
    "no previous value" in {
      Map("foo" -> 2).updateWith("bar", 1, _ + _) shouldReturn Map("foo" -> 2, "bar" -> 1)
    }
    "sequence" in {
      Map("foo" -> 2, "bar" -> 1)
          .updateWith(Seq("foo" -> 1, "bar" -> 3, "foo" -> 3, "bazz" -> 0, "bazz" -> 5, "quxx" -> 2), _ + _) shouldReturn
          Map("foo" -> 6, "bar" -> 4, "bazz" -> 5, "quxx" -> 2)
    }
    "merge" in {
      Map("foo" -> 1, "bar" -> 2).merge(Map("foo" -> 2, "bazz" -> 4), _ + _) shouldReturn
          Map("foo" -> 3, "bar" -> 2, "bazz" -> 4)
    }
  }
}
