package backend.lyrics

import backend.Url
import common.json.ToJsonableOps
import common.rich.RichT._
import common.rich.func.ToMoreMonadErrorOps
import controllers.ControllerUtils.songJsonable
import controllers.{ControllerUtils, LegacyController}
import models.Song
import play.api.mvc.{Action, Result}

import scalaz.std.FutureInstances

object LyricsController extends LegacyController
    with ToMoreMonadErrorOps with FutureInstances with ToJsonableOps {
  private val backend = new LyricsCache
  // TODO replace with Writable typeclass?
  private def toString(l: Lyrics): String = l.html + "<br><br>Source: " + l.source
  def get(path: String) = Action.async {
    backend.find(path.parseJsonable[Song])
        .map(toString)
        .orElse("Failed to get lyrics :(")
        .map(Ok(_))
  }
  def push(path: String) = Action.async { request =>
    val song = path.parseJsonable[Song]
    val url = request.body.asText.map(Url).get
    backend.parse(url, song)
        .map(toString)
        .orElse("Failed to parse lyrics")
        .map(Ok(_))
  }
  private def fromInstrumental(i: Instrumental): Result = Ok(i |> toString)
  def setInstrumentalSong(path: String) = Action.async {
    backend setInstrumentalSong path.parseJsonable[Song] map fromInstrumental
  }
  def setInstrumentalArtist(path: String) = Action.async {
    backend setInstrumentalArtist path.parseJsonable[Song] map fromInstrumental
  }
}
