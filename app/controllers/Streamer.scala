package controllers

import java.io.FileInputStream

import akka.stream.scaladsl.Source
import akka.util.ByteString
import common.rich.func.ToMoreFoldableOps
import common.rich.path.RichFile._
import common.rich.primitives.RichString._
import decoders.DbPowerampCodec
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.http.HttpEntity
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.mvc.Action

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.OptionInstances

object Streamer extends LegacyController
    with ToMoreFoldableOps with OptionInstances {
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]

  private val decoder = DbPowerampCodec
  def download(s: String) = Action.async {request =>
    // assumed format: [bytes=<start>-]
    def parseRange(s: String): Long = s.dropAfterLast('=').takeWhile(_.isDigit).toLong
    val needsEncoding = ControllerUtils.encodeMp3(request)
    val bytesToSkip = request.headers get "Range" map parseRange getOrElse 0L
    val file = ControllerUtils.parseSong(s).file.file
    (if (needsEncoding) decoder ! file else Future(file)).map(file => {
      val fis = new FileInputStream(file)
      fis.skip(bytesToSkip)
      val status = if (bytesToSkip == 0) Ok else PartialContent
      val source =
        Source.fromPublisher(IterateeStreams.enumeratorToPublisher(Enumerator fromStream fis)).map(ByteString.apply)
      val codec = if (needsEncoding || file.extension == "mp3") "audio/mpeg" else "audio/flac"
      status.sendEntity(HttpEntity.Streamed(source, Some(file.length - bytesToSkip), Some(codec))).withHeaders(
        "Access-Control-Allow-Headers" -> "range, accept-encoding",
        "Access-Control-Allow-Origin" -> "*",
        "Accept-Ranges" -> "bytes",
        "Connection" -> "close",
        "Codec" -> codec,
        "Content-Range" -> s"bytes $bytesToSkip-${file.length}/${file.length}",
      )
    })
  }

  // for debugging; plays the song in the browser instead of downloading it
  def playSong(s: String) = Action {
    Ok(views.html.playSong("/stream/download/" + s))
  }
}