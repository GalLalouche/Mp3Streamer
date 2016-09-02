package controllers

import java.io.FileInputStream

import common.rich.RichT._
import common.rich.primitives.RichString._
import decoders.DbPowerampCodec
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Streamer extends Controller {
  private val decoder = DbPowerampCodec
  def download(s: String) = Action.async { request =>
    // assumed format: [bytes=<start>-]
    def parseRange(s: String): Long = s dropAfterLast '=' takeWhile (_ isDigit) toLong
    val bytesToSkip = request.headers get "Range" map parseRange getOrElse 0L
    Future(Utils.parseSong(s).file |> decoder.encodeFileIfNeeded).map(file => {
      val fis = new FileInputStream(file)
      fis.skip(bytesToSkip)
      val status = if (bytesToSkip == 0) Ok else PartialContent
      // Deprecated my ass. Source, like everything else revolving akka, is an unusable, boiler-platy, steaming pile of shit.
      status.chunked(Enumerator fromStream fis).withHeaders(
      "Access-Control-Allow-Headers" -> "range, accept-encoding",
        "Access-Control-Allow-Origin" -> "*",
        "Accept-Ranges" -> "bytes",
        "Connection" -> "close",
        "Codec" -> "audio/mpeg",
        "Content-Type" -> "audio/mpeg",
        "Content-Length" -> (file.length - bytesToSkip).toString,
        "Content-Range" -> s"bytes $bytesToSkip-${file.length}/${file.length}"
      )
    })
  }

  // for debugging; plays the song in the browser instead of downloading it
  def playSong(s: String) = Action {
    Ok(views.html.playSong("/stream/download/" + s))
  }
}
