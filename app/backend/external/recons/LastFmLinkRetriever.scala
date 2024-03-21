package backend.external.recons

import java.net.HttpURLConnection
import javax.inject.Inject

import backend.FutureOption
import backend.external.{BaseLink, Host}
import backend.recon.Artist
import com.google.common.annotations.VisibleForTesting
import io.lemonlabs.uri.Url
import org.jsoup.Jsoup

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.OptionT
import scalaz.syntax.bind.ToBindOps

import common.RichJsoup._
import common.io.InternetTalker
import common.io.WSAliases._

private class LastFmLinkRetriever @VisibleForTesting private[recons] (
    it: InternetTalker,
    millisBetweenRedirects: Long,
) extends LinkRetriever[Artist] {
  override val qualityRank: Int = 1 // Not 0 since there might be another artist with the same name.
  @Inject() def this(it: InternetTalker) = this(it, millisBetweenRedirects = 100)

  override val host = Host.LastFm

  private implicit val iec: ExecutionContext = it
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
  }

  override def apply(a: Artist): FutureOption[BaseLink[Artist]] = OptionT {
    val url = Url.parse(s"https://www.last.fm/music/" + a.name.toLowerCase.replace(' ', '+'))
    it.get(url)
      .map(handleReply)
      .recoverWith {
        case _: TempRedirect =>
          Future(Thread.sleep(millisBetweenRedirects)) >> apply(a).run
        case e: MatchError =>
          Future.failed(
            new UnsupportedOperationException("last.fm returned an unsupported status code", e),
          )
      }
  }
}
