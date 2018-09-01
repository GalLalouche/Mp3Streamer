package backend.logging

import backend.configs.TestModuleConfiguration
import common.AuxSpecs
import common.io.{DirectoryRef, FileRef, RootDirectory}
import common.rich.collections.RichTraversableOnce._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.concurrent.ExecutionContext

class DirectoryLoggerTest extends FreeSpec with AuxSpecs with OneInstancePerTest {
  private val injector = TestModuleConfiguration().injector
  private val rootDirectory = injector.instance[DirectoryRef, RootDirectory]
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  private val logsDir = rootDirectory addSubDir "logs"
  private val $ = new DirectoryLogger(rootDirectory)

  "logging" in {
    $.info("foobar")
    def toFiles(xs: Traversable[LoggingLevel]): Traversable[FileRef] =
      logsDir.files.filter(e => xs.map(_.toString).exists(e.name.contains))
    val untouchedFiles = toFiles(Set(LoggingLevel.Warn, LoggingLevel.Error))
    assert(untouchedFiles.size == 2)
    for (file <- untouchedFiles)
      assert(file.lines.isEmpty, s"$file should have been empty")
    val touchedFiles = toFiles(Set(LoggingLevel.Verbose, LoggingLevel.Debug, LoggingLevel.Info))
    assert(touchedFiles.size == 3)
    for (file <- touchedFiles)
      assert(file.lines.single.contains("foobar"), s"$file should not have been empty")
  }
}
