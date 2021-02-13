package mains.cover.image

import backend.Url
import mains.cover.{ImageSource, UrlSource}
import play.api.libs.json.JsObject

import common.json.RichJson._

private object Parser {
  def apply(json: JsObject): Seq[ImageSource] = json.array("items").value.map {e =>
    val image = e./("image")
    UrlSource(Url(e str "link"), width = image int "width", height = image int "height")
  }
}
