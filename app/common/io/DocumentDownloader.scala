package common.io

import backend.Url
import backend.Retriever
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

object DocumentDownloader extends Retriever[Url, Document] {
  def download(u: Url)(implicit ec: ExecutionContext): Future[Document] =
    Future(Source.fromURL(u.address, "UTF-8"))
        .map(_.mkString)
        .map(Jsoup parse)
  override def apply(u: Url): Future[Document] = download(u)(ExecutionContext.Implicits.global)
}
