package controllers

import models.FakeModelFactory
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class PlayUrlTest extends FreeSpec with AuxSpecs {
  private val encoder = PlayUrlEncoder
  private val decoder = PlayUrlDecoder
  private val factory = new FakeModelFactory()
  "toJson" - {
    val song = factory.song(filePath = "foo + bar")
    "When + is present in the song, spaces are converted to %20" in {
      encoder(song) should endWith("foo%20%2B%20bar")
    }
    "decode" in {
      decoder(
        "d%3A%5Cmedia%5Cmusic%5CRock%5CClassic+Rock%5CBilly+Joel%5C07+-+Scenes+from+an+Italian+Restaurant.mp3",
      )
        .shouldReturn(
          """d:\media\music\Rock\Classic Rock\Billy Joel\07 - Scenes from an Italian Restaurant.mp3""",
        )
    }
    "can decode encoded song" in {
      decoder(encoder(song)) shouldReturn song.file.path
    }
  }
}
