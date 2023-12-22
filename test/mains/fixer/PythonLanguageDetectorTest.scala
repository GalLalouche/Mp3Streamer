package mains.fixer

import org.scalatest.FreeSpec

import common.test.AuxSpecs

class PythonLanguageDetectorTest extends FreeSpec with AuxSpecs {
  private val $ = PythonLanguageDetector.create()

  "Hebrew" in { $.detect("דגשדגשדגשדג") shouldReturn "he" }
  "Japanese" in { $.detect("センチメートル") shouldReturn "ja" }
  "Chinese" in { $.detect("汉英词典") shouldReturn "zh-cn" }
  "English" in { $.detect("This is a word") shouldReturn "en" }
}
