package backend.external

import java.io.InputStream
import java.net.{HttpURLConnection, URL}

import backend.Url
import backend.recon.Artist
import common.io.InternetTalker
import common.rich.RichT._
import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

private class LastFmReconciler(implicit ec: ExecutionContext, it: InternetTalker) extends Reconciler[Artist](Host.LastFm) {
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
    val httpConnection = a.name.toLowerCase.filterNot(_ == ' ')
        .mapTo(e => new URL(s"http://www.last.fm/music/$e").openConnection().asInstanceOf[HttpURLConnection])
    it connect httpConnection map handleReply
  }
}
