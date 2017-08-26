package backend.lyrics

import backend.Url
import backend.configs.Configuration
import common.rich.RichFuture._
import common.rich.RichT._
import controllers.Utils
import play.api.mvc.{Action, Controller, Result}

object LyricsController extends Controller {
  private implicit val c: Configuration = Utils.config

  private val backend = new LyricsCache
  // TODO replace with Writable typeclass?
  private def toString(l: Lyrics): String = l.html + "<br><br>Source: " + l.source
  def get(path: String) = Action.async {
    backend.find(Utils.parseSong(path))
        .map(toString)
        .orElse("Failed to get lyrics :(")
        .map(Ok(_))
  }
  def push(path: String) = Action.async {request =>
    val song = Utils.parseSong(path)
    val url = request.body.asText.map(Url).get
    backend.parse(url, song)
        .map(toString)
        .orElse("Failed to parse lyrics")
        .map(Ok(_))
  }
  private def fromInstrumental(i: Instrumental): Result = Ok(i |> toString)
  def setInstrumentalSong(path: String) = Action.async {
    backend setInstrumentalSong Utils.parseSong(path) map fromInstrumental
  }
  def setInstrumentalArtist(path: String) = Action.async {
    backend setInstrumentalArtist Utils.parseSong(path) map fromInstrumental
  }
}
