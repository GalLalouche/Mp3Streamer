package common

import java.io.File

import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process.stringSeqToProcess

import common.VimLauncher.VimLocation
import common.rich.path.RichFile.richFile

/** Launches vim as an external application for editing files. */
class VimLauncher @Inject() private (ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec

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
