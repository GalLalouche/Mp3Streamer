package controllers

import backend.Url
import backend.lyrics.{Lyrics, LyricsCache}
import common.rich.RichFuture._
import play.api.mvc.{Action, Controller}

object Lyrics extends Controller {
  import Utils.config

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
  def setInstrumental(path: String) = Action.async {
    backend.setInstrumental(Utils.parseSong(path))
        .map(toString)
        .map(Ok(_))
  }
}
