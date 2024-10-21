package mains.vimtag

import java.io.File

import backend.module.StandaloneModule
import com.google.inject.Guice
import mains.{IOUtils, JavaMainUtils}
import mains.vimtag.table.TableModule
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext
import scala.io.StdIn

import common.io.IODirectory
import common.rich.RichFuture._
import common.rich.collections.RichTraversableOnce._

object Main {
  private case class ExceptionAfterFileCreated(f: File, e: Exception) extends Exception(e)
  def main(args: Array[String]): Unit = try {
    JavaMainUtils.turnOffLogging()
    // TODO modules (lines/table) should come from args
    val injector = Guice.createInjector(StandaloneModule, TableModule)
    val vimEdit = injector.instance[VimEdit]
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val dir = IODirectory(IOUtils.decodeFile(args.view.single))
    val initialLines = injector.instance[Initializer].apply(dir)
    val (file, lines) = vimEdit(initialLines)
    try {
      lines
        .map(injector.instance[Parser].apply(initialLines.initialValues))
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
    case e: Throwable => handleException(e)
  }
  private def handleException(e: Throwable): Unit = {
    e.printStackTrace()
    StdIn.readLine()
  }
}
