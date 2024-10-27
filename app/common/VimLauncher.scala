package common

import java.io.File
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process.stringSeqToProcess

import common.rich.func.BetterFutureInstances.betterFutureInstances
import scalaz.Scalaz.ToBindOps

import common.VimLauncher.VimLocation
import common.rich.path.RichFile.richFile

/** Utilities for editing files using vim as an external application. */
class VimLauncher @Inject() private (ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec

  /**
   * Creates a temporary file containing the input lines, and returns a [[Future]] which will
   * complete when the editor has been closed.
   */
  def withLines(
      lines: Seq[String],
      fileSuffix: String = "",
      additionalCommandLineArgs: Seq[String] = Nil,
  ): Future[Seq[String]] = for {
    temp <- Future(File.createTempFile("vim_temp_file", fileSuffix))
    result <- Future(temp.write(lines.mkString("\n"))) >> withFile(temp, additionalCommandLineArgs)
  } yield {
    temp.deleteOnExit()
    result
  }

  /**
   * If a file already exists, just return a future which will complete when the editor has been
   * closed. Also useful if you want to hold the file later on.
   */
  def withFile(file: File, additionalCommandlineArgs: Seq[String]): Future[Seq[String]] = Future {
    Vector(VimLocation + additionalCommandlineArgs.mkString(" ", " ", ""), file.path).!!
    file.lines.toVector
  }
}

private object VimLauncher {
  private val VimLocation = """C:\Program Files\Neovim\bin\nvim-qt.exe"""
}
