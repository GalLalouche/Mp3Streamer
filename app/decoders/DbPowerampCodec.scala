package decoders

import java.io.File

import backend.logging.ConsoleLogger
import common.Debug
import common.io.{FileRef, IODirectory}
import common.rich.path.RichFile._
import decoders.CodecType.CodecType

import scala.sys.process.{Process, ProcessLogger}

// TODO make a class, pass logger, put in ControllerUtils
// TODO make its extra accept a Song
private object DbPowerampCodec extends Mp3Encoder(IODirectory("D:/media/streamer/musicOutput")) with Debug {
  // Do this less hackishly
  private val converterFile = new File("D:/Media/Tools/dBpoweramp/CoreConverter.exe")
  private def quote(o: Any): String = s""""$o""""

  private implicit val logger: ConsoleLogger = new ConsoleLogger
  override def encode(srcFile: FileRef, dstFile: FileRef, dstType: CodecType): Unit = {
    // create the arguments for the application invocation
    val args = List(converterFile.path,
      "-infile=" + quote(srcFile.path),
      "-outfile=" + quote(dstFile.path),
      "-convert_to=" + quote(dstType),
      "-V 2",
      "-b 320")
    timed(s"Encoding $srcFile to $dstType") {
      Process(args) !< devNull
    }
  }
  private val devNull = new ProcessLogger {
    // sends all output to FREAKING NOWHERE
    // can't use !! because it throws an exception from the decoder for some reason
    override def out(s: => String): Unit = {}
    override def err(s: => String): Unit = {}

    override def buffer[T](f: => T): T = f
  }
}
