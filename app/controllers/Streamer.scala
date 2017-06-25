package controllers

import java.io.FileInputStream

import common.rich.path.RichFile._
import common.rich.primitives.RichString._
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, Controller}

object Streamer extends Controller {
  import Utils.config
  def download(s: String) = Action { request =>
    // assumed format: [bytes=<start>-]
    def parseRange(s: String): Long = s dropAfterLast '=' takeWhile (_ isDigit) toLong
    val bytesToSkip = request.headers get "Range" map parseRange getOrElse 0L
    val file = Utils.parseSong(s).iofile
    val codec = if(file.extension == "flac") "audio/x-flac" else "audio/mpeg"
    val fis = new FileInputStream(file)
    fis.skip(bytesToSkip)
    val status = if (bytesToSkip == 0) Ok else PartialContent
    // Deprecated my ass. Source, like everything else revolving akka, is an unusable, boiler-platy, steaming pile of shit.
    status.chunked(Enumerator fromStream fis).withHeaders(
      "Access-Control-Allow-Headers" -> "range, accept-encoding",
      "Access-Control-Allow-Origin" -> "*",
      "Accept-Ranges" -> "bytes",
      "Connection" -> "close",
      "Codec" -> codec,
      "Content-Type" -> codec,
      "Content-Length" -> (file.length - bytesToSkip).toString,
      "Content-Range" -> s"bytes $bytesToSkip-${file.length}/${file.length}"
    )
  }

  // for debugging; plays the song in the browser instead of downloading it
  def playSong(s: String) = Action {
    Ok(views.html.playSong("/stream/download/" + s))
  }
}
