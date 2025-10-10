package common

import common.test.AuxSpecs
import org.scalatest.FreeSpec

class PipeSubjectTest extends FreeSpec with AuxSpecs {
  "apply" in {
    val $ = PipeSubject[String, Int](_.length)
    var x = 0
    $.doOnNext(x = _).subscribe()
    $.onNext("foo")
    x shouldReturn 3
  }
}
