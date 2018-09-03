package backend.lyrics

import backend.Url
import common.rich.RichT._
import common.rich.func.ToMoreMonadErrorOps
import controllers.ControllerUtils
import javax.inject.Inject
import play.api.mvc.{InjectedController, Result}

import scala.concurrent.ExecutionContext

import scalaz.std.FutureInstances

class LyricsController @Inject()(ec: ExecutionContext, backend: LyricsCache) extends InjectedController
    with ToMoreMonadErrorOps with FutureInstances {
  private implicit val iec: ExecutionContext = ec
  // TODO replace with Writable typeclass?
  private def toString(l: Lyrics): String = l.html + "<br><br>Source: " + l.source
  def get(path: String) = Action.async {
    backend.find(ControllerUtils parseSong path)
        .map(toString)
        .orElse("Failed to get lyrics :(")
        .map(Ok(_))
  }
  def push(path: String) = Action.async {request =>
    val song = ControllerUtils parseSong path
    val url = request.body.asText.map(Url).get
    backend.parse(url, song)
        .map(toString)
        .orElse("Failed to parse lyrics")
        .map(Ok(_))
  }
  private def fromInstrumental(i: Instrumental): Result = Ok(i |> toString)
  def setInstrumentalSong(path: String) = Action.async {
    backend setInstrumentalSong ControllerUtils.parseSong(path) map fromInstrumental
  }
  def setInstrumentalArtist(path: String) = Action.async {
    backend setInstrumentalArtist ControllerUtils.parseSong(path) map fromInstrumental
  }
}
