package common.io

import backend.Url
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

object DocumentDownloader {
  def apply(u: Url)(implicit ec: ExecutionContext): Future[Document] =
    Future(Source.fromURL(u.address, "UTF-8"))
        .map(_.mkString)
        .map(Jsoup parse)
}
