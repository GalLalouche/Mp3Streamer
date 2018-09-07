package decoders

import java.time.LocalDateTime

import backend.RichTime.OrderingLocalDateTime._
import common.concurrency.{Extra, SimpleTypedActor}
import common.io.{DirectoryRef, FileRef, RootDirectory}
import javax.inject.Inject

/** Encodes audio files files to mp3. Also handles caching */
class Mp3Encoder @Inject()(@RootDirectory rootDirectory: DirectoryRef, encoder: Encoder)
    extends SimpleTypedActor[FileRef, FileRef] {
  private val outputDir = rootDirectory addSubDir "musicOutput"
  private val cleanOldFiles = new Extra {
    override def apply(): Unit = {
      val minimumCreationTime: LocalDateTime = LocalDateTime.now.minusWeeks(1)
      outputDir.files.filter(_.creationTime < minimumCreationTime).foreach(_.delete)
    }
  }

  private def encodeFileIfNeeded(f: FileRef): FileRef = if (f.extension.toLowerCase != "mp3") encode(f) else f

  /**
   * Encode the file to an mp3 format. The file will only be created if its matching doesn't already exist.
   * @param file The file to decode
   * @return The (possibly new) mp3 file created; The file will be created in the outputDir, and will
   *         be the absolute path of the file (with no space) with .mp3
   */
  private def encode(file: FileRef): FileRef = {
    require(file != null)
    require(file.exists)
    cleanOldFiles.!()
    val outputFileName = file.path.replaceAll("[\\s\\/\\\\\\-\\:]", "").toLowerCase + ".mp3"
    outputDir.files.find(_.name == outputFileName).filter(_.size > 0).getOrElse({
      val outputFile = outputDir.addFile(outputFileName)
      encoder.encode(file, outputFile, CodecType.Mp3)
      outputFile
    })
  }

  override val unique = true
  override def apply(m: FileRef): FileRef = encodeFileIfNeeded(m)
}

