package backend.lyrics

import backend.Url
import backend.lyrics.retrievers.RetrievedLyricsResult
import controllers.UrlPathUtils
import javax.inject.Inject
import models.Song
import play.twirl.api.utils.StringEscapeUtils

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import common.rich.func.ToMoreMonadErrorOps._

private class LyricsFormatter @Inject()(
    ec: ExecutionContext,
    backend: LyricsCache,
    urlPathUtils: UrlPathUtils,
) {
  private implicit val iec: ExecutionContext = ec

  // TODO replace with Writable typeclass?
  private def toString(l: Lyrics): String = l.html + "<br><br>Source: " + l.source
  def get(path: String): Future[String] = backend.find(urlPathUtils parseSong path)
      .map(toString)
      //.listenError(_.printStackTrace())
      .orElse("Failed to get lyrics :(")
  def push(path: String, url: Url): Future[String] = backend.parse(url, urlPathUtils parseSong path).map {
    case RetrievedLyricsResult.RetrievedLyrics(l) => toString(l)
    case RetrievedLyricsResult.Error(e) => StringEscapeUtils.escapeXml11(e.getMessage)
    case RetrievedLyricsResult.NoLyrics => "No lyrics were found :("
  }
  private def setInstrumentalAux(path: String, f: Song => Future[Instrumental]) =
    f(urlPathUtils.parseSong(path)).map(toString)
  def setInstrumentalSong(path: String): Future[String] =
    setInstrumentalAux(path, backend.setInstrumentalSong)
  def setInstrumentalArtist(path: String): Future[String] =
    setInstrumentalAux(path, backend.setInstrumentalArtist)
}
