package decoders;

import java.io.File

import scala.sys.process.{ Process, ProcessLogger }

import DbPowerampCodec.devNull
import common.Debug
import common.rich.path.RichPath.richPath
import decoders.CodecType.CodecType

trait DbPowerampCodec extends Encoder with Debug {
	val codecPath: String;

	private implicit def richString(o: Any) = new {
		def wrapInQuotes(): String = "\"%s\"".format(o)
	}

	override def encode(srcFile: File, dstFile: File, dstType: CodecType, otherCommands: List[String]) {
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

object DbPowerampCodec {
	private val devNull = new ProcessLogger { // sends all output to FREAKING NOWHERE
		// cann't use !! because it throws an exception from the decoder for some reason
		override def out(s: => String): Unit = {}
		override def err(s: => String): Unit = {}

		override def buffer[T](f: => T): T = {
			f
		}
	}
}
