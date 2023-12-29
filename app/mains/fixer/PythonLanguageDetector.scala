package mains.fixer

import java.io.{File, IOException}
import java.nio.charset.StandardCharsets

import mains.fixer.PythonLanguageDetector.Encoding

import scala.io.Source

import common.rich.RichT.richT
import common.rich.primitives.RichBoolean.richBoolean

/** Wraps a python process for detecting languages, so multiple calls are cheaper. */
private class PythonLanguageDetector private {
  def detect(s: String): String = {
    val outStream = process.getOutputStream
    outStream.write(s"$s\n".getBytes(Encoding))
    if (process.getErrorStream.available() > 0)
      readErrorAndFail
    outStream.flush()
    val lines = Source.fromInputStream(process.getInputStream).getLines()
    if (lines.hasNext.isFalse)
      readErrorAndFail
    lines.next()
  }

  private def readErrorAndFail = throw new IOException(
    "Python code failed: " +
      Source.fromInputStream(process.getErrorStream).getLines().mkString("\n"),
  )

  private val process: Process =
    new ProcessBuilder()
      .command(
        "python",
        new File(getClass.getResource("language_detector.py").toURI).getAbsolutePath,
      )
      .<|(_.environment().put("PYTHONIOENCODING", Encoding.toString))
      .start()
}

private object PythonLanguageDetector {
  /**
   * Creating a new detector initializes a new Python process, so this can be expensive, relatively
   * speaking (~200 msec).
   */
  def create(): PythonLanguageDetector = new PythonLanguageDetector

  private val Encoding = StandardCharsets.UTF_8
}
