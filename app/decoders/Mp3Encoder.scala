package decoders

import java.io.{File, IOException}
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

import common.concurrency.Extra
import common.rich.path.Directory
import common.rich.path.RichFile.{poorFile, richFile}
import org.joda.time.DateTime

/** Encodes audio files files to mp3. Also handles caching */
abstract class Mp3Encoder(outputDir: Directory) extends Encoder {
  val cleanOldFiles = new Extra {
    override def apply() {
      def getCreationTime(f: File) =
        Files.readAttributes(f.toPath, classOf[BasicFileAttributes]).creationTime().toMillis
      val minimumCreationTime = DateTime.now.minusWeeks(1).getMillis
      outputDir.files.filter(getCreationTime(_) < minimumCreationTime).foreach(_.delete)
    }
  }

  def encodeFileIfNeeded(f: File) = if (f.extension.toLowerCase != "mp3") encode(f) else f

  /**
   * Encode the file to an mp3 format. The file will only be created if its matching doesn't already exist.
   * @param file The file to decode
   * @return The (possibly new) mp3 file created; The file will be created in the outputDir, and will
   *         be the absolute path of the file (with no space) with .mp3
   * @throws IOException
   */
  def encode(file: File): File = {
    require(file != null)
    require(file.exists)
    require(file.isDirectory == false)
    cleanOldFiles.!()
    val outputFileName = file.path.replaceAll("[\\s\\/\\\\\\-\\:]", "").toLowerCase + ".mp3"
    outputDir.files.find(_.name == outputFileName).filter(_.length > 0).getOrElse({
      val outputFile = outputDir.addFile(outputFileName)
      encode(file, outputFile, CodecType.Mp3)
      outputFile
    })
  }
}

