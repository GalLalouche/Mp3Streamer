package controllers

import java.io.FileInputStream

import akka.stream.scaladsl.Source
import akka.util.ByteString
import common.rich.path.RichFile._
import common.rich.primitives.RichString._
import play.api.http.HttpEntity
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.mvc.Action

object Streamer extends LegacyController {
  import ControllerUtils.config

  def download(s: String) = Action {request =>
    // assumed format: [bytes=<start>-]
    val bytesToSkip = request.headers get "Range" map (_ dropAfterLast '=' takeWhile (_ isDigit) toLong) getOrElse 0L
    val file = ControllerUtils.parseSong(s).file.file
    val fis = new FileInputStream(file)
    fis.skip(bytesToSkip)
    val status = if (bytesToSkip == 0) Ok else PartialContent
    val source =
      Source.fromPublisher(IterateeStreams.enumeratorToPublisher(Enumerator fromStream fis)).map(ByteString.apply)
    val codec = if (file.extension == "flac") "audio/x-flac" else "audio/mpeg"
    status.sendEntity(HttpEntity.Streamed(source, Some(file.length - bytesToSkip), Some(codec))).withHeaders(
        "Access-Control-Allow-Headers" -> "range, accept-encoding",
        "Access-Control-Allow-Origin" -> "*",
        "Accept-Ranges" -> "bytes",
        "Connection" -> "close",
        "Content-Range" -> s"bytes $bytesToSkip-${file.length}/${file.length}"
    )
  }

  // for debugging; plays the song in the browser instead of downloading it
  def playSong(s: String) = Action {
    Ok(views.html.playSong("/stream/download/" + s))
  }
}
