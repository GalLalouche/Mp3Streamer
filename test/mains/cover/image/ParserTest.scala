package mains.cover.image

import io.lemonlabs.uri.Url
import mains.cover.UrlSource
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json.{JsObject, Json}

import common.test.AuxSpecs

class ParserTest extends AnyFreeSpec with AuxSpecs {
  "apply" in {
    Parser(Json.parse(getClass.getResourceAsStream("test.json")).as[JsObject])
      .shouldContainExactly(
        UrlSource(
          Url.parse("https://upload.wikimedia.org/wikipedia/en/a/af/Opeth_Orchid.jpg"),
          width = 400,
          height = 300,
        ),
        UrlSource(
          Url.parse("https://img.discogs.com/discogs-images/R-5076240-1460985218-7752.jpeg.jpg"),
          width = 600,
          height = 600,
        ),
      )
  }
}
