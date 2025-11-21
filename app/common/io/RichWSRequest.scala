package common.io

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

import common.io.WSAliases._
import common.rich.RichT._

object RichWSRequest {
  implicit class richWSResponse($ : WSRequest)(implicit ec: ExecutionContext) {
    def bytes: Future[Array[Byte]] = $.get.map(_.bodyAsBytes.toArray[Byte])
    def document(decodeUtf: Boolean): Future[Document] =
      string.map(_.mapIf(decodeUtf).to(UtfDecoder(_)) |> Jsoup.parse)
    def string: Future[String] = bytes.map(new String(_, "UTF-8"))
  }
}
