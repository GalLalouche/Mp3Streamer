package backend.lyrics

import backend.Url
import backend.lyrics.retrievers.RetrievedLyricsResult
import controllers.UrlPathUtils
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.{-\/, \/-}
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
  def get(path: String): Future[String] =
    backend.find(urlPathUtils parseSong path)
        .map(toString)
        .listenError(_.printStackTrace())
        .orElse("Failed to get lyrics :(")
  def push(path: String, url: Url): Future[String] = {
    val song = urlPathUtils parseSong path
    backend.parse(url, song).mapEitherMessage({
      case RetrievedLyricsResult.RetrievedLyrics(l) => \/-(toString(l))
      case _ => -\/("Failed to parse lyrics :(")
    })
  }
  def setInstrumentalSong(path: String): Future[String] =
    backend.setInstrumentalSong(urlPathUtils.parseSong(path)).map(toString)
  def setInstrumentalArtist(path: String): Future[String] =
    backend.setInstrumentalArtist(urlPathUtils.parseSong(path)).map(toString)
}
