package mains.cover.image

import io.lemonlabs.uri.Url
import mains.cover.{ImageSource, UrlSource}
import play.api.libs.json.JsObject

import common.json.RichJson.DynamicJson

private object Parser {
  // Despite Scrappa's documentation claiming otherwise, the JSON returned by the API is identical
  // to that of SerpAPI.
  def apply(json: JsObject): ImageSource = UrlSource(
    Url.parse(json.str("original")),
    width = json.int("original_width"),
    height = json.int("original_height"),
  )
}
