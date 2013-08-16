package decoders;

import java.io.File
import scala.sys.process.ProcessLogger
import decoders.CodecType._
import common.Path.richPath
import common.Debug
import java.io.File
import scala.sys.process.Process
/**
  * An implementation of a codec using dBpoweramp appliocation
  *
  * @author Gal Lalouche
  */
trait DbPowerampCodec extends Codec with Debug {
	val codecPath: String;

	private implicit def richString(o: Any) = new {
		def wrapInQuotes = "\"%s\"".format(o)
	}
	val devNull = new ProcessLogger { // sends all output to FREAKING NOWHERE
		// cann't use !! because it throws an exception from the decoder for some reason
		override def out(s: => String): Unit = {}
		override def err(s: => String): Unit = {}

		override def buffer[T](f: => T): T = {
			f
		}
	}
	override def decode(srcFile: File, dstFile: File, dstType: CodecType, otherCommands: List[String]) {
		// create the arguments for the application invocation
		val args = List(codecPath.wrapInQuotes,
			"-infile=" + srcFile.path.wrapInQuotes,
			"-outfile=" + dstFile.path.wrapInQuotes,
			"-convert_to=" + dstType.wrapInQuotes) ++
			otherCommands
		timed("Decoding %s to %s".format(srcFile, dstType)) {
			val p = Process(args) !< (devNull)
		}
	}
}
