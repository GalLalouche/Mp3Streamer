package common.io

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

import common.io.WSAliases._
import common.rich.RichT._

object RichWSRequest {
  implicit class richWSResponse($ : WSRequest)(implicit ec: ExecutionContext) {
    private def mapGet[T](f: WSResponse => T): Future[T] = $.get().map(f)
    def bytes: Future[Array[Byte]] = mapGet(_.bodyAsBytes.toArray[Byte])
    def document(decodeUtf: Boolean): Future[Document] =
      string.map(_.mapIf(decodeUtf).to(UtfDecoder(_)) |> Jsoup.parse)
    def string: Future[String] = bytes.map(new String(_, "UTF-8"))
  }
}
