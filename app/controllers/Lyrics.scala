package controllers

import common.rich.RichFuture._
import lyrics.LyricsCache
import play.api.mvc.{Action, Controller}

object Lyrics extends Controller {
  private implicit val config = PlayConfig
  import config._
  
  private val lyrics = new LyricsCache()
  def get(path: String) = Action.async {
    lyrics.apply(Utils.parseSong(path))
        .map(l => l.html + "<br><br>Source: " + l.source)
        .orElse("Failed to get lyrics :(")
        .map(Ok(_))
  }
}
