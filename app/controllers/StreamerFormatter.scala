package controllers

import javax.inject.Inject

import decoders.Mp3Encoder
import play.api.mvc.InjectedController

import scala.concurrent.{ExecutionContext, Future}

import common.io.IOFile

private class StreamerFormatter @Inject() (
    encoder: Mp3Encoder,
    helper: FileStreamFormatter,
    ec: ExecutionContext,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def apply(path: String, range: Option[String], needsEncoding: Boolean): Future[StreamResult] = {
    val file = IOFile(path)
    val codec = if (needsEncoding || file.extension == "mp3") "audio/mpeg" else "audio/flac"
    val maybeEncodedFile = if (needsEncoding) encoder ! file else Future.successful(file)
    maybeEncodedFile.map(helper(_, codec, range).withHeaders("Codec" -> codec))
  }
}
