package controllers

import backend.lyrics.LyricsCache
import common.rich.RichFuture._
import play.api.mvc.{Action, Controller}

object Lyrics extends Controller {
  private implicit val config = PlayConfig

  private val lyrics = new LyricsCache()
  def get(path: String) = Action.async {
    lyrics.apply(Utils.parseSong(path))
        .map(l => l.html + "<br><br>Source: " + l.source)
        .orElse("Failed to get lyrics :(")
        .map(Ok(_))
  }
}
