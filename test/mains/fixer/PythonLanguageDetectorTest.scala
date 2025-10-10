package mains.fixer

import org.scalatest.EitherValues._
import org.scalatest.FreeSpec
import org.scalatest.tagobjects.Slow

import scala.concurrent.duration.DurationInt

import common.test.AuxSpecs

class PythonLanguageDetectorTest extends FreeSpec with AuxSpecs {
  "Single process" - {
    lazy val $ = PythonLanguageDetector.create(1.minute)
    // TODO Proper
    def detect(s: String) = $.detect(s).right.value
    "Hebrew" in { detect("דגשדגשדגשדג") shouldReturn "he" }
    "Japanese" in { detect("センチメートル") shouldReturn "ja" }
    "Chinese" in { detect("汉英词典") shouldReturn "zh-cn" }
    "English" in { detect("This is a word") shouldReturn "en" }
    "Creation count" in {
      $.creationCount.get shouldReturn 1
    }
  }

  "Multiple processes" - {
    lazy val $ = PythonLanguageDetector.create(1.millisecond)
    def detect(s: String) = $.detect(s).right.value
    "Hebrew" taggedAs Slow in { detect("דגשדגשדגשדג") shouldReturn "he" }
    "Japanese" taggedAs Slow in { detect("センチメートル") shouldReturn "ja" }
    "Chinese" taggedAs Slow in { detect("汉英词典") shouldReturn "zh-cn" }
    "English" taggedAs Slow in { detect("This is a word") shouldReturn "en" }
    "Creation count" taggedAs Slow in { // Not really slow, but shouldn't run if the others didn't.
      $.creationCount.get shouldReturn 4
    }
  }
}
