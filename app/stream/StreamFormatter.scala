package stream

import javax.inject.Inject

import musicfinder.MusicFinder
import song_encoder.Mp3Encoder

import scala.concurrent.{ExecutionContext, Future}

import common.io.{BaseDirectory, DirectoryRef, IOFile}

class StreamFormatter @Inject() (
    encoder: Mp3Encoder,
    ec: ExecutionContext,
    mf: MusicFinder,
    @BaseDirectory baseDir: DirectoryRef,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(path: String, range: Option[String], needsEncoding: Boolean): Future[StreamResult] = {
    val file = IOFile(path)
    require(
      baseDir.isDescendant(path),
      s"Can only download posters from the music directory <${baseDir.path}>, but path was <$path>",
    )
    require(
      mf.extensions.contains(file.extension),
      s"Can only download music files, but file had extension <${file.extension}>",
    )
    val codec = if (needsEncoding || file.extension == "mp3") "audio/mpeg" else "audio/flac"
    val maybeEncodedFile = if (needsEncoding) encoder ! file else Future.successful(file)
    maybeEncodedFile.map(FileStreamer(_, codec, range).addHeaders("Codec" -> codec))
  }
}
