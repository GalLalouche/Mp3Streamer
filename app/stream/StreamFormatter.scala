package stream

import java.io.File

import com.google.inject.Inject
import musicfinder.SongFileFinder
import song_encoder.Mp3Encoder

import scala.concurrent.{ExecutionContext, Future}

import common.io.{FileDownloadValidator, IOFile}

class StreamFormatter @Inject() (
    encoder: Mp3Encoder,
    ec: ExecutionContext,
    sff: SongFileFinder,
    fileDownloadValidator: FileDownloadValidator,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(path: String, range: Option[String], needsEncoding: Boolean): Future[StreamResult] = {
    fileDownloadValidator(new File(path), sff.extensions)
    val file = IOFile(path)
    val codec = if (needsEncoding || file.extension == "mp3") "audio/mpeg" else "audio/flac"
    val maybeEncodedFile = if (needsEncoding) encoder ! file else Future.successful(file)
    maybeEncodedFile.map(FileStreamer(_, codec, range).addHeaders("Codec" -> codec))
  }
}
