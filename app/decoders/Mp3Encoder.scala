package decoders;

import java.io.File

import common.rich.path.Directory
import common.rich.path.RichFile._

/**
  * Encodes audio files files to mp3.
  * Also handles caching
  *
  * @author Gal Lalouche
  */
trait Mp3Encoder extends Codec {
	val outputDir: Directory

	/**
	  * Encode the file to an mp3 format
	  *
	  * @param originalFile The file to decode
	  * @return The new mp3 file created; The file will be created in the {@code outputDir}, and will be the absolute
	  *         path of the file (with no space) with .mp3
	  * @throws IOException
	  */
	def decode(file: File): File = {
		require(file != null)
		require(file exists)
		require(file.isDirectory == false)
		val outputFileName = file.path.replaceAll("[\\s\\/\\\\\\-\\:]", "").toLowerCase + ".mp3";
		outputDir.files.find(_.name == outputFileName).getOrElse({
			val outputFile = outputDir.addFile(outputFileName)
			decode(file, outputFile, CodecType.Mp3, List("-V 2", "-b 320"));
			outputFile;
		})
	}
}

