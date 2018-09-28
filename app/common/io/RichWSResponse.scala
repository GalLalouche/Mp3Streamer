package common.io

import common.io.WSAliases._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object RichWSResponse {
  implicit class richWSResponse($: WSResponse) {
    def bytes: Array[Byte] = $.bodyAsBytes.toArray
    def string: String = new String(bytes, "UTF-8")
    def document: Document = Jsoup.parse(string)
  }
}
