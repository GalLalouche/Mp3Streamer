package mains.vimtag

import java.io.File

import com.google.inject.Inject
import mains.vimtag.Initializer.InitialLines
import mains.vimtag.VimEdit._

import scala.concurrent.{ExecutionContext, Future}

import common.VimLauncher
import common.rich.path.RichFile.richFile
import common.rich.primitives.RichString._

private class VimEdit @Inject() (
    cp: CommandsProvider,
    launcher: VimLauncher,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  def apply(initialLines: InitialLines): (File, Future[Seq[String]]) = {
    val file = File.createTempFile("vimEdit", ".id3tbl")
    file.write(initialLines.lines.mkString("\n"))
    (file, makeArgs(initialLines.startingEditLine).flatMap(launcher.withFile(file, _)))
  }

  private def makeArgs(startingEditLine: Int): Future[Seq[String]] = Future {
    val tempVimCode = SetupBindings.createFile()
    val loadMacros = Vector("--", "-S", tempVimCode.getAbsolutePath.quote)
    val commands = cp.get ++ Vector(NormalCommand(s"${startingEditLine}G"))
    commands.map(_.asCommandString) ++ loadMacros
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
