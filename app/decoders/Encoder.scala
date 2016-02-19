package decoders

import java.io.File
import decoders.CodecType._

trait Encoder {
	/**
	  * Decodes an audio file to a different format
	  *
	  * @param srcFile The original file
	  * @param dstFile The destination file that will be created
	  * @param dstType The type of the new file
	  * @param otherCommands Any other commands sent to the codec
	  * @throws IOException
	  */
	protected def encode(srcFile: File, dstFile: File, dstType: CodecType)
}
