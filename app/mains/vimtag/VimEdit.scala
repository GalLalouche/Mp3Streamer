package mains.vimtag

import java.io.File

import common.rich.path.RichFile._
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

private class VimEdit @Inject()(implicit ec: ExecutionContext) {
  private val VimLocation = """"C:\Program Files (x86)\Vim\vim74\gvim.exe""""
  private val FormattedCommands = Vector(
    "winpos 0 0", // Start in the corner
    "set lines=1000", // Max height
    "set columns=210", // About half a screen's width
  ).map(command => s"""-c ":$command"""").mkString(" ", " ", "")

  def apply(initialLines: Seq[String]): (File, Future[Seq[String]]) = {
    val temp = File.createTempFile("vimedit", "")
    temp.write(initialLines mkString "\n")
    temp -> inFile(temp)
  }
  def inFile(file: File): Future[Seq[String]] = Future {
    Seq(VimLocation + FormattedCommands, file.path).!!
    file.lines.filterNot(_ startsWith "#").toVector
  }
}
