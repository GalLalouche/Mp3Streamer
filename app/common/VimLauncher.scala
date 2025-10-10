package common

import java.io.File

import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process.stringSeqToProcess

import cats.syntax.flatMap.catsSyntaxFlatMapOps

import common.VimLauncher.VimLocation
import common.rich.path.RichFile.richFile

/** Launches vim as an external application for editing files. */
class VimLauncher @Inject() private (ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec

  /**
   * Creates a temporary file containing the input lines, and returns a [[Future]] containing the
   * file lines which will complete when the editor has been closed.
   */
  def withLines(lines: Seq[String], fileSuffix: String): Future[Seq[String]] = for {
    temp <- Future(File.createTempFile("vim_temp_file", fileSuffix))
    result <- Future(temp.write(lines.mkString("\n"))) >> withFile(temp, Nil)
  } yield {
    temp.deleteOnExit()
    result
  }

  /**
   * If a file already exists, just return a [[Future]] containing the file lines which will
   * complete when the editor has been closed. Also useful if you want to hold the file later on.
   */
  def withFile(file: File, additionalCommandlineArgs: Seq[String]): Future[Seq[String]] = Future {
    (VimLocation +: additionalCommandlineArgs :+ file.path).!!
    file.lines.toVector
  }
}

private object VimLauncher {
  private val VimLocation = """C:\Program Files\neovim-qt 0.2.19\bin\nvim-qt.exe"""
}
