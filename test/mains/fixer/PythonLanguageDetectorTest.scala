package mains.fixer

import org.scalatest.EitherValues._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.tagobjects.Slow

import scala.concurrent.duration.DurationInt

import common.test.AuxSpecs

class PythonLanguageDetectorTest extends AnyFreeSpec with AuxSpecs {
  "Single process" - {
    lazy val $ = PythonLanguageDetector.create(1.minute)
    // TODO Proper
    def detect(s: String) = $.detect(s).value
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
    def detect(s: String) = {
      val result = $.detect(s).value
      // Sleep to ensure the 1ms idle-monitor has had time to kill the previous process.
      // Without this, under CPU load, the monitor thread may not get scheduled between test cases,
      // causing detect() to reuse the still-alive process instead of creating a new one.
      Thread.sleep(50)
      result
    }
    "Hebrew" taggedAs Slow in { detect("דגשדגשדגשדג") shouldReturn "he" }
    "Japanese" taggedAs Slow in { detect("センチメートル") shouldReturn "ja" }
    "Chinese" taggedAs Slow in { detect("汉英词典") shouldReturn "zh-cn" }
    "English" taggedAs Slow in { detect("This is a word") shouldReturn "en" }
    "Creation count" taggedAs Slow in { // Not really slow, but shouldn't run if the others didn't.
      $.creationCount.get shouldReturn 4
    }
  }
}
