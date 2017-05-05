package controllers

import backend.Url
import backend.lyrics.LyricsCache
import common.rich.RichFuture._
import play.api.mvc.{Action, Controller}

object Lyrics extends Controller {
  import Utils.config

  private val backend = new LyricsCache()
  def get(path: String) = Action.async {
    backend.find(Utils.parseSong(path))
        .map(l => l.html + "<br><br>Source: " + l.source)
        .orElse("Failed to get lyrics :(")
        .map(Ok(_))
  }
  def push(path: String) = Action.async {request =>
    val song = Utils.parseSong(path)
    val url = request.body.asText.map(Url).get
    backend.parse(url, song)
        .map(l => l.html + "<br><br>Source: " + l.source)
        .orElse("Failed to parse lyrics")
        .map(Ok(_))
  }
}
