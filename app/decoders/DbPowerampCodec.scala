package decoders

import java.io.File

import common.Debug
import common.concurrency.SimpleTypedActor
import common.rich.path.Directory
import common.rich.path.RichPath.richPath
import decoders.CodecType.CodecType

import scala.sys.process.{Process, ProcessLogger}

object DbPowerampCodec extends Mp3Encoder(Directory("D:/media/streamer/musicOutput")) with Debug with SimpleTypedActor[File, File] {
  val codecFile = new File("D:/Media/Tools/dBpoweramp/CoreConverter.exe")
  private implicit class richString(o: Any) {
    def quote: String = '"' + o.toString + '"'
  }

  override def encode(srcFile: File, dstFile: File, dstType: CodecType) {
    // create the arguments for the application invocation
    val args = List(codecFile.path,
      "-infile=" + srcFile.path.quote,
      "-outfile=" + dstFile.path.quote,
      "-convert_to=" + dstType.quote,
      "-V 2",
      "-b 320")
    timed(s"Decoding $srcFile $dstType") {
      val p = Process(args) !< devNull
    }
  }
  override def apply(m: File): File = encodeFileIfNeeded(m)
  private val devNull = new ProcessLogger {
    // sends all output to FREAKING NOWHERE
    // can't use !! because it throws an exception from the decoder for some reason
    override def out(s: => String): Unit = {}
    override def err(s: => String): Unit = {}

    override def buffer[T](f: => T): T = {
      f
    }
  }
}
