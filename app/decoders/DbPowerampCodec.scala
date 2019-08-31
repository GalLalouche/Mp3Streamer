package decoders

import java.io.{File, IOException}

import backend.logging.Logger
import javax.inject.Inject

import scala.sys.process.{Process, ProcessLogger}

import common.Debug
import common.io.{FileRef, IOFile}
import common.rich.path.RichFile._

private class DbPowerampCodec @Inject()(implicit logger: Logger) extends Encoder with Debug {
  // Do this less hackishly
  private val converterFile = new File("D:/Media/Tools/dBpoweramp/CoreConverter.exe")
  private def quote(o: Any): String = s""""$o""""

  override def encode(srcFile: FileRef, dstFile: FileRef, dstType: CodecType): Unit = dstType match {
    case Mp3 =>
      val args = List(converterFile.path,
        "-infile=" + quote(srcFile.path),
        "-outfile=" + quote(dstFile.path),
        "-convert_to=" + quote("mp3 (lame)"),
        "-V 2",
        "-b 320")
      timed(s"Encoding $srcFile to $dstType") {
        if (Process(args) !< devNull != 0)
          throw new IOException("DbPowerAmp failed to convert file")
      }
      TagCopier(srcFile.asInstanceOf[IOFile], dstFile.asInstanceOf[IOFile])
    case Flac => ???
  }
  private val devNull = new ProcessLogger {
    // sends all output to FREAKING NOWHERE
    // can't use !! because it throws an exception from the decoder for some reason
    override def out(s: => String): Unit = {}
    override def err(s: => String): Unit = {}

    override def buffer[T](f: => T): T = f
  }
}
