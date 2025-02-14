package stream

import javax.inject.Inject

import song_encoder.Mp3Encoder

import scala.concurrent.{ExecutionContext, Future}

import common.io.IOFile

class StreamerFormatter @Inject() (encoder: Mp3Encoder, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec
  def apply(path: String, range: Option[String], needsEncoding: Boolean): Future[StreamResult] = {
    val file = IOFile(path)
    val codec = if (needsEncoding || file.extension == "mp3") "audio/mpeg" else "audio/flac"
    val maybeEncodedFile = if (needsEncoding) encoder ! file else Future.successful(file)
    maybeEncodedFile.map(FileStreamer(_, codec, range).withHeaders("Codec" -> codec))
  }
}
