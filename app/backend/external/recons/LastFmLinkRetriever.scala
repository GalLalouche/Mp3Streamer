package backend.external.recons

import java.net.HttpURLConnection

import backend.{FutureOption, Url}
import backend.external.{BaseLink, Host}
import backend.recon.Artist
import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind.ToBindOps

import common.io.InternetTalker
import common.io.WSAliases._

private class LastFmLinkRetriever @VisibleForTesting private[recons](
    it: InternetTalker, millisBetweenRedirects: Long
) extends LinkRetriever[Artist] {
  @Inject() def this(it: InternetTalker) = this(it, millisBetweenRedirects = 100)

  override val host = Host.LastFm

  private implicit val iec: ExecutionContext = it
  private class TempRedirect extends Exception
  private def handleReply(h: WSResponse): Option[BaseLink[Artist]] = h.status match {
    case HttpURLConnection.HTTP_NOT_FOUND => None
    case HttpURLConnection.HTTP_MOVED_TEMP => throw new TempRedirect
    case HttpURLConnection.HTTP_OK =>
      Jsoup.parse(h.body)
          .select("link").asScala
          .find(_.attr("rel") == "canonical")
          .map(_.attr("href"))
          .filter(_.nonEmpty)
          .map(e => BaseLink[Artist](Url(e), Host.LastFm))
  }

  override def apply(a: Artist): FutureOption[BaseLink[Artist]] = {
    val url = Url(s"https://www.last.fm/music/" + a.name.toLowerCase.replace(' ', '+'))
    it.get(url) map handleReply recoverWith {
      case _: TempRedirect => Future(Thread sleep millisBetweenRedirects).>>(apply(a))
      case e: MatchError => Future.failed(new UnsupportedOperationException("last.fm returned an unsupported status code", e))
    }
  }
}
