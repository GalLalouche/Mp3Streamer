package common.io

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.{WSRequest, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

object RichWSRequest {
  implicit class richWSResponse($: WSRequest)(implicit ec: ExecutionContext) {
    private def mapGet[T](f: WSResponse => T): Future[T] = $.get() map f
    def bytes: Future[Array[Byte]] = mapGet(_.bodyAsBytes.toArray[Byte])
    def document: Future[Document] = string map Jsoup.parse
    def string: Future[String] = mapGet(_.body)
  }
}
