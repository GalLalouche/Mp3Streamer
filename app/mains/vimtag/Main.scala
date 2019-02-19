package mains.vimtag

import java.io.File

import backend.module.StandaloneModule
import com.google.inject.Guice
import common.io.IODirectory
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Main {
  private case class ExceptionAfterFileCreated(f: File, e: Exception) extends Exception(e)
  def main(args: Array[String]): Unit = try {
    val injector = Guice.createInjector(StandaloneModule)
    val vimEdit = injector.instance[VimEdit]
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val default = """E:\Incoming\Bittorrent\Completed\Music\Beethoven Complete Symphonies - Karajan [SACD 6CD FLAC]\CD1 1&2"""
    val dir = IODirectory(if (args.isEmpty) default else args(0))
    val (file, lines) = vimEdit(injector.instance[Initializer].apply(dir))
    try {
      lines
          .map(Parser.apply)
          .map(Fixer(dir, _))
          .get
      file.deleteOnExit()
      file.delete()
    } catch {
      case e: Exception => throw ExceptionAfterFileCreated(file, e)
    }
  } catch {
    case ExceptionAfterFileCreated(file, e) =>
      println(s"Temporary file at: <$file>")
      handleException(e)
    case e: Exception => handleException(e)
  }
  private def handleException(e: Exception): Unit = {
    e.printStackTrace()
    StdIn.readLine()
  }
}
