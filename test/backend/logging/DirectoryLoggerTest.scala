package backend.logging

import backend.configs.{Configuration, TestConfiguration}
import common.AuxSpecs
import common.io.FileRef
import common.rich.collections.RichTraversableOnce._
import org.scalatest.{FreeSpec, OneInstancePerTest}

class DirectoryLoggerTest extends FreeSpec with AuxSpecs with OneInstancePerTest {
  private implicit val c: Configuration = new TestConfiguration
  private val $ = new DirectoryLogger
  private val logsDir = c.rootDirectory addSubDir "logs"

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
