package common.io

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.{JsArray, JsObject, Json}

import common.io.WSAliases._
import common.rich.RichT.richT

object RichWSResponse {
  implicit class richWSResponse($ : WSResponse) {
    def bytes: Array[Byte] = $.bodyAsBytes.toArray
    def document(decodeUtf: Boolean): Document =
      Jsoup.parse(string.mapIf(decodeUtf).to(UtfDecoder(_)))
    def string: String = new String(bytes, "UTF-8")
    def jsonObject: JsObject = Json.parse(string).as[JsObject]
    def jsonArray: JsArray = Json.parse(string).as[JsArray]
    def document: Document = Jsoup.parse(string)
  }
}
