package common.io

import common.io.WSAliases._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

object RichWSRequest {
  implicit class richWSResponse($: WSRequest)(implicit ec: ExecutionContext) {
    private def mapGet[T](f: WSResponse => T): Future[T] = $.get() map f
    def bytes: Future[Array[Byte]] = mapGet(_.bodyAsBytes.toArray[Byte])
    def document: Future[Document] = string map Jsoup.parse
    def string: Future[String] = mapGet(_.body)
  }
}
