package backend.lyrics

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import backend.lyrics.retrievers.RetrievedLyricsResult
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._
import controllers.UrlPathUtils
import io.lemonlabs.uri.Url
import models.Song
import play.twirl.api.utils.StringEscapeUtils

private class LyricsFormatter @Inject() (
    ec: ExecutionContext,
    backend: LyricsCache,
    urlPathUtils: UrlPathUtils,
) {
  private implicit val iec: ExecutionContext = ec

  def get(path: String): Future[String] = backend
    .find(urlPathUtils.parseSong(path))
    .map(LyricsFormatter.toString)
    // .listenError(_.printStackTrace())
    .orElse("Failed to get lyrics :(")
  def push(path: String, url: Url): Future[String] =
    backend.parse(url, urlPathUtils.parseSong(path)).map {
      case RetrievedLyricsResult.RetrievedLyrics(l) => LyricsFormatter.toString(l)
      case RetrievedLyricsResult.Error(e) => StringEscapeUtils.escapeXml11(e.getMessage)
      case RetrievedLyricsResult.NoLyrics => "No lyrics were found :("
    }
  private def setInstrumentalAux(path: String, f: Song => Future[Instrumental]) =
    f(urlPathUtils.parseSong(path)).map(LyricsFormatter.toString)
  def setInstrumentalSong(path: String): Future[String] =
    setInstrumentalAux(path, backend.setInstrumentalSong)
  def setInstrumentalArtist(path: String): Future[String] =
    setInstrumentalAux(path, backend.setInstrumentalArtist)
}

private object LyricsFormatter {
  // TODO replace with Writable typeclass?
  private def encodeUrl(sourceName: String): LyricsUrl => String = {
    case LyricsUrl.Url(url: io.lemonlabs.uri.Url) =>
      s"""<a href="${url.toStringPunycode}" target="_blank">$sourceName</a>"""
    case _ => sourceName
  }
  private def toString(l: Lyrics): String = l.html + "<br><br>Source: " + encodeUrl(l.source)(l.url)
}
