package backend.lyrics

import backend.Url
import backend.lyrics.retrievers.RetrievedLyricsResult
import common.rich.RichT._
import common.rich.func.ToMoreMonadErrorOps
import controllers.UrlPathUtils
import javax.inject.Inject
import play.api.mvc.{InjectedController, Result}

import scala.concurrent.ExecutionContext

import scalaz.{-\/, \/-}
import scalaz.std.FutureInstances

class LyricsController @Inject()(
    ec: ExecutionContext,
    backend: LyricsCache,
    urlPathUtils: UrlPathUtils,
) extends InjectedController
    with ToMoreMonadErrorOps with FutureInstances {
  private implicit val iec: ExecutionContext = ec
  // TODO replace with Writable typeclass?
  private def toString(l: Lyrics): String = l.html + "<br><br>Source: " + l.source
  def get(path: String) = Action.async {
    backend.find(urlPathUtils parseSong path)
        .map(toString)
        .listenError(_.printStackTrace())
        .orElse("Failed to get lyrics :(")
        .map(Ok(_))
  }
  def push(path: String) = Action.async {request =>
    val song = urlPathUtils parseSong path
    val url = request.body.asText.map(Url).get
    backend.parse(url, song).mapEitherMessage({
      case RetrievedLyricsResult.RetrievedLyrics(l) => \/-(toString(l))
      case _ => -\/("Failed to parse lyrics :(")
    }).map(Ok(_))
  }
  private def fromInstrumental(i: Instrumental): Result = Ok(i |> toString)
  def setInstrumentalSong(path: String) = Action.async {
    backend setInstrumentalSong urlPathUtils.parseSong(path) map fromInstrumental
  }
  def setInstrumentalArtist(path: String) = Action.async {
    backend setInstrumentalArtist urlPathUtils.parseSong(path) map fromInstrumental
  }
}
