package controllers

import lyrics.LyricsCache
import play.api.mvc.{Action, Controller}
import java.io.File
import java.net.URLDecoder

import models.Song
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import common.RichFuture._

object Lyrics extends Controller {
  private val lyrics = LyricsCache
  def get(path: String) = Action.async {
    val s = Song(new File(URLDecoder.decode(path, "UTF-8")))
    lyrics.get(s).map(_.html).orElse("Failed to get lyrics :(").map(Ok(_))
  }
}
