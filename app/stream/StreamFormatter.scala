package stream

import java.io.File

import com.google.inject.Inject
import musicfinder.SongFileFinder

import scala.concurrent.ExecutionContext

import common.io.FileDownloadValidator
import common.path.ref.io.IOFile

class StreamFormatter @Inject() (
    ec: ExecutionContext,
    sff: SongFileFinder,
    fileDownloadValidator: FileDownloadValidator,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(path: String, range: Option[String]): StreamResult = {
    fileDownloadValidator(new File(path), sff.extensions)
    val file = IOFile(path)
    val codec = if (file.hasExtension("mp3")) "audio/mpeg" else "audio/flac"
    FileStreamer(file, codec, range).addHeaders("Codec" -> codec)
  }
}
