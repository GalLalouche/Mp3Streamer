package common.io

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.{JsObject, Json}

import common.io.WSAliases._
import common.rich.RichT.richT

object RichWSResponse {
  implicit class richWSResponse($ : WSResponse) {
    def bytes: Array[Byte] = $.bodyAsBytes.toArray
    def document(decodeUtf: Boolean): Document =
      Jsoup.parse(string.mapIf(decodeUtf).to(UtfDecoder(_)))
    def string: String = new String(bytes, "UTF-8")
    def jsonObject: JsObject = Json.parse(string).as[JsObject]
    def document: Document = Jsoup.parse(string)
  }
}
