package mains.cover.image

import io.lemonlabs.uri.Url
import mains.cover.{ImageSource, UrlSource}
import play.api.libs.json.JsObject

import common.json.RichJson._

private object Parser {
  def apply(json: JsObject): Seq[ImageSource] = {
    if (json.has("error"))
      throw new Exception("API error: " + json./("error").str("message"))
    json
      .array("items")
      .map { e =>
        val image = e./("image")
        UrlSource(
          Url.parse(e.str("link")),
          width = image.int("width"),
          height = image.int("height"),
        )
      }
  }
}
