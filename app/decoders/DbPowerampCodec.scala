package decoders;

import java.io.File
import scala.sys.process.{ Process, ProcessLogger }
import DbPowerampCodec.devNull
import common.Debug
import common.rich.path.RichPath.richPath
import decoders.CodecType.CodecType
import common.rich.path.Directory

class DbPowerampCodec(codecFile: File, outputDir: Directory) extends Mp3Encoder(outputDir) with Debug {
  private implicit class richString(o: Any) {
    def quote: String = s""""$o""""";
  }

  override def encode(srcFile: File, dstFile: File, dstType: CodecType) {
    // create the arguments for the application invocation
    val args = List(codecFile.path.quote,
      "-infile=" + srcFile.path.quote,
      "-outfile=" + dstFile.path.quote,
      "-convert_to=" + dstType.quote,
      "-V 2",
      "-b 320")
    timed(s"Decoding $srcFile $dstType") {
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
