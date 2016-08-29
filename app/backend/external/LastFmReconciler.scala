package backend.external

import java.io.InputStream
import java.net.{HttpURLConnection, URL}

import backend.Url
import backend.configs.CleanConfiguration
import backend.recon.Artist
import common.io.InternetTalker

import scala.concurrent.{ExecutionContext, Future}
import common.rich.RichFuture._
import common.rich.RichT._
import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.io.Source

class LastFmReconciler(implicit ec: ExecutionContext, it: InternetTalker) extends Reconciler[Artist] {
  private def handleReply(h: HttpURLConnection): Option[ExternalLink[Artist]] = h.getResponseCode match {
    case HttpURLConnection.HTTP_NOT_FOUND => None
    case HttpURLConnection.HTTP_OK =>
      Jsoup.parse(h.getContent.asInstanceOf[InputStream], h.getContentEncoding, h.getURL.getFile)
          .select("link")
          .find(_.attr("rel") == "canonical")
          .map(_.attr("href"))
          .filter(_.nonEmpty)
          .map(e => ExternalLink[Artist](Url(e), Host.LastFm))
  }
  override def apply(a: Artist): Future[Option[ExternalLink[Artist]]] = {
    val h = new URL("http://www.last.fm/music/" + a.name.toLowerCase.filterNot(_ == ' ')).openConnection().asInstanceOf[HttpURLConnection]
    //    h setInstanceFollowRedirects false
    it connect h map handleReply
    //    val connection: HttpURLConnection = it.connect(h)
    //    kk
    //    println(connection.getContent.asInstanceOf[InputStream].mapTo(Source.fromInputStream).mapTo(_.getLines().toList.mkString("\n")))
    //    Future successful None
  }
}

object LastFmReconciler {
  def main(args: Array[String]): Unit = {
    implicit val c = CleanConfiguration
    val $ = new LastFmReconciler()
    $.apply(Artist("dreamtheater")).get.log()
  }
}
