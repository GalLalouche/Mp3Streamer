package decoders;

import java.io.File
import common.rich.path.Directory
import common.rich.path.RichFile.{ poorFile, richFile }
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import org.joda.time.DateTime
import common.DaemonRunner

/** Encodes audio files files to mp3. Also handles caching */
abstract class Mp3Encoder(outputDir: Directory) extends Encoder {
	private def cleanOldFiles() {
	  def getCreationTime(f: File) = Files.readAttributes(f.toPath, classOf[BasicFileAttributes]).creationTime().toMillis()
	  val minimumCreationTime = DateTime.now.minusWeeks(1).getMillis
	  outputDir.files.filter(getCreationTime(_) < minimumCreationTime).foreach(_.delete)
	}
	
	def encodeFileIfNeeded(f: File) = if (f.extension.toLowerCase != "mp3") encode(f) else f
	
	/**
	  * Encode the file to an mp3 format. The file will only be created if its matching doesn't already exist.
	  *
	  * @param originalFile The file to decode
	  * @return The (possibly new) mp3 file created; The file will be created in the {@code outputDir}, and will 
	  * 				be the absolute path of the file (with no space) with .mp3
	  * @throws IOException
	  */
	def encode(file: File): File = {
		require(file != null)
		require(file exists)
		require(file.isDirectory == false)
		DaemonRunner run cleanOldFiles
		val outputFileName = file.path.replaceAll("[\\s\\/\\\\\\-\\:]", "").toLowerCase + ".mp3";
		outputDir.files.find(_.name == outputFileName).getOrElse({
			val outputFile = outputDir.addFile(outputFileName)
			encode(file, outputFile, CodecType.Mp3);
			outputFile;
		})
	}
}

