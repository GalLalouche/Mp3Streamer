package mains.vimtag

import java.io.File
import javax.inject.Inject

import mains.vimtag.Initializer.InitialLines
import mains.vimtag.VimEdit._

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

import common.rich.path.RichFile._
import common.rich.primitives.RichString._

private class VimEdit @Inject() (cp: CommandsProvider, ec: ExecutionContext) {
  private val VimLocation = """"C:\Program Files\Neovim\bin\nvim-qt.exe""""
  private implicit val iec: ExecutionContext = ec

  def apply(initialLines: InitialLines): (File, Future[Seq[String]]) = {
    val temp = File.createTempFile("vimedit", "")
    temp.write(initialLines.lines.mkString("\n"))
    (temp, inFile(temp, initialLines.startingEditLine))
  }
  private def inFile(file: File, startingEditLine: Int): Future[Seq[String]] = Future {
    val tempVimCode = SetupBindings.createFile()
    val loadMacros = Vector("--", "-S", tempVimCode.getAbsolutePath.quote)
    val commands = cp.get ++ Vector(NormalCommand(s"${startingEditLine}G"))
    val formattedCommands = (commands.map(_.asCommandString) ++ loadMacros).mkString(" ", " ", "")

    Vector(VimLocation + formattedCommands, file.path).!!
    file.lines.toVector
  }
}

private object VimEdit {
  sealed trait Command {
    def asCommandString: String
  }
  case class NormalCommand(s: String) extends Command {
    override def asCommandString = s""""+norm $s""""
  }
  case class ExecutionCommand(s: String) extends Command {
    override def asCommandString = s"""+":$s""""
  }
}
