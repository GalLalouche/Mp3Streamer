package mains.cover.image

import io.lemonlabs.uri.Url
import mains.cover.{ImageSource, UrlSource}
import play.api.libs.json.JsObject

import common.json.RichJson._

private object Parser {
  def apply(json: JsObject): Seq[ImageSource] = {
    if (json.has("error"))
      throw new Exception("API error: " + json.str("error"))
    json
      .array("images_results")
      .map { e =>
        UrlSource(
          Url.parse(e.str("original")),
          width = e.int("original_width"),
          height = e.int("original_height"),
        )
      }
  }
}
