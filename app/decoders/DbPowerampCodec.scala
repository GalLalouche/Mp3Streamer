package decoders

import java.io.File

import backend.logging.ConsoleLogger
import common.Debug
import common.concurrency.SimpleTypedActor
import common.rich.path.Directory
import common.rich.path.RichFile._
import decoders.CodecType.CodecType

import scala.concurrent.Future
import scala.sys.process.{Process, ProcessLogger}

// TODO make a class, pass logger, put in ControllerUtils
// TODO make its extra accept a Song
object DbPowerampCodec extends Mp3Encoder(Directory("D:/media/streamer/musicOutput")) with Debug with SimpleTypedActor[File, File] {
  // Do this less hackishly
  override val unique = true
  private val converterFile = new File("D:/Media/Tools/dBpoweramp/CoreConverter.exe")
  private def quote(o: Any): String = s""""$o""""

  private implicit val logger: ConsoleLogger = new ConsoleLogger
  override def encode(srcFile: File, dstFile: File, dstType: CodecType) {
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
  override def !(m: => File): Future[File] = super.!(m.getAbsoluteFile)
}
