package controllers

import decoders.Mp3Encoder
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.{ExecutionContext, Future}

private class StreamerFormatter @Inject()(
    ec: ExecutionContext,
    encoder: Mp3Encoder,
    helper: FileStreamFormatter,
    urlPathUtils: UrlPathUtils,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def apply(path: String, range: Option[String], needsEncoding: Boolean): Future[StreamResult] = {
    val file = urlPathUtils.parseSong(path).file
    val codec = if (needsEncoding || file.extension == "mp3") "audio/mpeg" else "audio/flac"
    val maybeEncodedFile = if (needsEncoding) encoder ! file else Future.successful(file)
    maybeEncodedFile.map(helper(_, codec, range).withHeaders("Codec" -> codec))
  }
}