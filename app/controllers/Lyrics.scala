package controllers

import common.RichFuture._
import lyrics.LyricsCache
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}

object Lyrics extends Controller {
  private val lyrics = new LyricsCache
  def get(path: String) = Action.async {
    lyrics.get(Utils.parseSong(path))
        .map(l => l.html + "<br><br>Source: " + l.source)
        .orElse("Failed to get lyrics :(")
        .map(Ok(_))
  }
}
