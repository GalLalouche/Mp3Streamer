package backend.lyrics.retrievers.genius

import models.FakeModelFactory
import org.scalatest.FreeSpec
import org.scalatest.OptionValues._
import play.api.libs.json.{JsObject, Json}

import common.test.AuxSpecs

class APITest extends FreeSpec with AuxSpecs {
  private val factory = new FakeModelFactory
  "parse" - {
    "valid" in {
      val json = Json.parse(getClass.getResourceAsStream("search_result.json")).as[JsObject]
      API.parse(factory.song(artistName = "Wormwood", title = "Sunnas Hadanfard"), json)
          .value shouldReturn "https://genius.com/Wormwood-sunnas-hadanfard-lyrics"
    }
    "returns none on no hits" in {
      val emptyJson =
        """
          |{
          |  "meta": {
          |    "status": 200
          |  },
          |  "response": {
          |    "hits": []
          |  }
          |}
          |""".stripMargin
      val json = Json.parse(emptyJson).as[JsObject]
      API.parse(factory.song(), json) shouldReturn None
    }
    "returns none when artist name does not match" in {
      val json = Json.parse(getClass.getResourceAsStream("search_result.json")).as[JsObject]
      API.parse(factory.song(artistName = "NotWormwood", title = "Sunnas Hadanfard"), json) shouldReturn None
    }
    "returns none when title does not match" in {
      val json = Json.parse(getClass.getResourceAsStream("search_result.json")).as[JsObject]
      API.parse(factory.song(artistName = "Wormwood", title = "Not Sunnas Hadanfard"), json) shouldReturn None
    }
  }
}
