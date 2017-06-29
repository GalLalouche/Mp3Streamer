package backend.external.recons

import java.io.InputStream
import java.net.{HttpURLConnection, URL}

import backend.Url
import backend.external.{ExternalLink, Host}
import backend.recon.Artist
import common.io.InternetTalker
import common.rich.RichT._
import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

private class LastFmReconciler(millisBetweenRedirects: Long = 100)
    (implicit ec: ExecutionContext, it: InternetTalker) extends Reconciler[Artist](Host.LastFm)
    with ToBindOps with FutureInstances {
  private class TempRedirect extends Exception
  private def handleReply(h: HttpURLConnection): Option[ExternalLink[Artist]] = h.getResponseCode match {
    case HttpURLConnection.HTTP_NOT_FOUND => None
    case HttpURLConnection.HTTP_MOVED_TEMP => throw new TempRedirect
    case HttpURLConnection.HTTP_OK =>
      Jsoup.parse(h.getContent.asInstanceOf[InputStream], h.getContentEncoding, h.getURL.getFile)
          .select("link")
          .find(_.attr("rel") == "canonical")
          .map(_.attr("href"))
          .filter(_.nonEmpty)
          .map(e => ExternalLink[Artist](Url(e), Host.LastFm))
  }

  override def apply(a: Artist): Future[Option[ExternalLink[Artist]]] = {
    val url = Url(s"https://www.last.fm/music/" + a.name.toLowerCase.replaceAll(" ", "+"))
    it connect url map handleReply recoverWith {
      case _: TempRedirect => Future(Thread sleep millisBetweenRedirects).>>(apply(a))
      case e: MatchError => Future.failed(new UnsupportedOperationException("last.fm returned an unsupported status code", e))
    }
  }
}
