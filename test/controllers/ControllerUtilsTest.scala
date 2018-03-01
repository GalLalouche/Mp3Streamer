package controllers

import common.AuxSpecs
import models.FakeModelFactory
import org.scalatest.FreeSpec

class ControllerUtilsTest extends FreeSpec with AuxSpecs {
  private val $ = ControllerUtils
  private val factory = new FakeModelFactory()
  "toJson" - {
    val song = factory.song(filePath = "foo + bar")
    "When + is present in the song, spaces are converted to %20" in {
      $.encode(song) should endWith("foo%20%2B%20bar")
    }
    "decode" in {
      $.decode("d%3A%5Cmedia%5Cmusic%5CRock%5CClassic+Rock%5CBilly+Joel%5C07+-+Scenes+from+an+Italian+Restaurant.mp3")
          .shouldReturn("""d:\media\music\Rock\Classic Rock\Billy Joel\07 - Scenes from an Italian Restaurant.mp3""")
    }
    "can decode encoded song" in {
      $.decode($.encode(song)) shouldReturn song.file.path
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
