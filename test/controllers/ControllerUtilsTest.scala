package controllers

import common.AuxSpecs
import models.FakeModelFactory
import org.scalatest.FreeSpec

class ControllerUtilsTest extends FreeSpec with AuxSpecs {
  private val $ = ControllerUtils
  private val factory = new FakeModelFactory()
  "toJson" - {
    "When + is present in the song, spaces are converted to %20" in {
      $.encode(factory.song("foo + bar")) should endWith("foo%20%2B%20bar")
    }
  }
  "parseFile" - {
    "with + in file name" - {
      "as +" in $.parseFile("Foo%20+%20Bar").toString.shouldReturn("Foo + Bar")
      "as %2B" in $.parseFile("Foo%20%2B%20Bar").toString.shouldReturn("Foo + Bar")
      "as %2B when string contains +" in $.parseFile("Foo+%2B+Bar").toString.shouldReturn("Foo + Bar")
      "without %2B it should mean space" in $.parseFile("Foo+Bar").toString.shouldReturn("Foo Bar")
    }
  }
}
