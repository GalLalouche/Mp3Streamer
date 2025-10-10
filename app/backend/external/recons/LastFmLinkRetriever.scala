package backend.external.recons

import java.net.HttpURLConnection

import backend.FutureOption
import backend.external.{BaseLink, Host}
import backend.recon.Artist
import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import org.jsoup.Jsoup

import scala.concurrent.{ExecutionContext, Future}

import cats.data.OptionT
import cats.implicits.catsSyntaxFlatMapOps

import common.RichJsoup._
import common.io.InternetTalker
import common.io.WSAliases._

private class LastFmLinkRetriever @VisibleForTesting private[recons] (
    it: InternetTalker,
    millisBetweenRedirects: Long,
    ec: ExecutionContext,
) extends LinkRetriever[Artist] {
  private implicit val iec: ExecutionContext = ec
  override val qualityRank: Int = 1 // Not 0 since there might be another artist with the same name.
  @Inject() def this(it: InternetTalker, ec: ExecutionContext) =
    this(it, millisBetweenRedirects = 100, ec)

  override val host = Host.LastFm

  private class TempRedirect extends Exception

  private def handleReply(h: WSResponse): Option[BaseLink[Artist]] = h.status match {
    case HttpURLConnection.HTTP_NOT_FOUND => None
    case HttpURLConnection.HTTP_MOVED_TEMP => throw new TempRedirect
    case HttpURLConnection.HTTP_OK =>
      Jsoup
        .parse(h.body)
        .find("link[rel=canonical]")
        .map(_.href)
        .filter(_.nonEmpty)
        .map(e => BaseLink[Artist](Url.parse(e), Host.LastFm))
    case e if e >= HttpURLConnection.HTTP_INTERNAL_ERROR =>
      scribe.error(s"last.fm returned a 500 error code <$e>")
      None
  }

  override def apply(a: Artist): FutureOption[BaseLink[Artist]] = OptionT {
    val url = Url.parse(s"https://www.last.fm/music/" + a.name.toLowerCase.replace(' ', '+'))
    it.get(url)
      .map(handleReply)
      .recoverWith {
        case _: TempRedirect =>
          Future(Thread.sleep(millisBetweenRedirects)) >> apply(a).value
        case e: MatchError =>
          Future.failed(
            new UnsupportedOperationException("last.fm returned an unsupported status code", e),
          )
      }
  }
}
