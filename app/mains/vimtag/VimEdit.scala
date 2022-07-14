package mains.vimtag

import javax.inject.Inject
import mains.vimtag.Initializer.InitialLines
import mains.vimtag.VimEdit._

import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

import common.rich.path.RichFile._

private class VimEdit @Inject()(cp: CommandsProvider, ec: ExecutionContext) {
  private val VimLocation = """"C:\Program Files (x86)\Vim\vim74\gvim.exe""""
  private implicit val iec: ExecutionContext = ec

  def apply(initialLines: InitialLines): (File, Future[Seq[String]], Map[String, InitialValues]) = {
    val temp = File.createTempFile("vimedit", "")
    temp.write(initialLines.lines mkString "\n")
    (temp, inFile(temp, initialLines.startingEditLine), initialLines.initialValues)
  }
  private def inFile(file: File, startingEditLine: Int): Future[Seq[String]] = Future {
    val commands = Vector(
      ExecutionCommand("winpos 0 0"), // Start in the corner
      ExecutionCommand("set lines=1000"), // Max height
      ExecutionCommand(s"set columns=${cp.width}"), // Screen's width

    ) ++ cp.get ++ Vector(NormalCommand(s"${startingEditLine}G"))
    val formattedCommands = commands.map(_.asCommandString).mkString(" ", " ", "")
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
    override def asCommandString = s"""-c :"$s""""
  }
}
