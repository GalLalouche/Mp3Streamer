package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import common.rich.path.RichFile._
import models.{Poster, Song}
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.AnyContent
import search.ModelsJsonable.SongJsonifier

private object Utils {
  def toJson(s: Song): JsObject = SongJsonifier.jsonify(s) +
      ("poster" -> JsString("/posters/" + Poster.getCoverArt(s).path)) +
      (s.file.extension -> JsString("/stream/download/" + URLEncoder.encode(s.file.path, "UTF-8")))
  def parseSong(path: String): Song = Song(parseFile(path))
  def parseFile(path: String): File = new File(URLDecoder.decode(path, "UTF-8"))

  def getStringFromBody(a: AnyContent): String = a.asFormUrlEncoded.get.keys.head
}
