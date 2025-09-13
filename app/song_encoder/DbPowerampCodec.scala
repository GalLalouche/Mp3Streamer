package song_encoder

import java.io.{File, IOException}

import com.google.inject.Inject
import song_encoder.DbPowerampCodec.{quote, ConverterFile, DevNull}

import scala.sys.process.{Process, ProcessLogger}

import common.TimedLogger
import common.io.{FileRef, IOFile}
import common.rich.path.RichFile._

private class DbPowerampCodec @Inject() (timed: TimedLogger) extends SongEncoder {
  override def encode(srcFile: FileRef, dstFile: FileRef): Unit = {
    val args = Vector(
      ConverterFile.path,
      "-infile=" + quote(srcFile.path),
      "-outfile=" + quote(dstFile.path),
      "-convert_to=" + quote("mp3 (lame)"),
      "-V 2",
      "-b 320",
    )
    timed(s"Encoding $srcFile to MP3") {
      if (Process(args) !< DevNull != 0)
        throw new IOException("DbPowerAmp failed to convert file")
    }
    TagCopier(srcFile.asInstanceOf[IOFile].file, dstFile.asInstanceOf[IOFile].file)
  }
}

private object DbPowerampCodec {
  private val ConverterFile = new File("G:/Media/Tools/dBpoweramp/CoreConverter.exe")
  private def quote(o: Any): String = s""""$o""""

  private val DevNull = new ProcessLogger {
    // sends all output to FREAKING NOWHERE
    // can't use !! because it throws an exception from the decoder for some reason
    override def out(s: => String): Unit = {}
    override def err(s: => String): Unit = {}

    override def buffer[T](f: => T): T = f
  }
}
