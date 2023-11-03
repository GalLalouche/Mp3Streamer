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
import common.rich.collections.RichTraversableOnce._
import common.rich.RichFuture._

object Main {
  private case class ExceptionAfterFileCreated(f: File, e: Exception) extends Exception(e)
  def main(args: Array[String]): Unit = try {
    JavaMainUtils.turnOffLogging()
    // TODO modules (lines/table) should come from args
    val injector = Guice.createInjector(StandaloneModule, TableModule)
    val vimEdit = injector.instance[VimEdit]
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val dir = IODirectory(IOUtils.decodeFile(args.view.single))
    val (file, lines, initialValues) = vimEdit(injector.instance[Initializer].apply(dir))
    try {
      lines
        .map(injector.instance[Parser].apply(initialValues))
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
