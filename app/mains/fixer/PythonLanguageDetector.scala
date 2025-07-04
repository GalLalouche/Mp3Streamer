package mains.fixer

import java.io.{File, IOException}
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import com.google.common.annotations.VisibleForTesting
import mains.fixer.PythonLanguageDetector.Encoding

import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.Try
import scala.util.control.Breaks.{break, breakable}

import scalaz.Scalaz.{eitherInstance, ToBifunctorOps}

import common.rich.RichInputStream.richInputStream
import common.rich.RichT.richT
import common.rich.primitives.RichBoolean.richBoolean

/**
 * Wraps a Python process for detecting languages, so multiple calls are cheaper. The Python process
 * is killed after being idle for timeout span.
 */
private class PythonLanguageDetector private (timeout: Duration) {
  @VisibleForTesting private[fixer] val creationCount = new AtomicInteger()
  private val lastUsed = new AtomicLong(System.currentTimeMillis)
  private var process: Process = createProcess()

  def detect(s: String): Either[IOException, String] = Try(synchronized {
    lastUsed.set(System.currentTimeMillis)
    if (process.isAlive.isFalse)
      process = createProcess()
    val outStream = process.getOutputStream
    outStream.write(s"$s\n".getBytes(Encoding))
    if (process.getErrorStream.available() > 0)
      return readErrorAndFail
    outStream.flush()
    val lines = Source.fromInputStream(process.getInputStream).getLines()
    if (lines.hasNext.isFalse)
      return readErrorAndFail
    lines.next()
  }).toEither.leftMap {
    case io: IOException => io
    case e: Throwable => throw e
  }

  private def readErrorAndFail = Left(
    new IOException(
      "Python code failed: " +
        Source.fromInputStream(process.getErrorStream).getLines().mkString("\n"),
    ),
  )

  private def createProcess(): Process = {
    creationCount.incrementAndGet()
    scribe.trace("Creating new Python language detection process")
    val tempFile = File.createTempFile("language_detector", ".py").<|(_.deleteOnExit())
    getClass.getResourceAsStream("language_detector.py").writeTo(tempFile)
    val $ = new ProcessBuilder()
      .command("python", tempFile.getAbsolutePath)
      .<|(_.environment().put("PYTHONIOENCODING", Encoding.toString))
      .start()
    // Monitor thread to kill the process after enough idle time has passed. This isn't done in
    // the Python code, since, "surprisingly", waiting on input with a timeout is a PITA.
    new Thread(() =>
      breakable {
        while (true) {
          val beforeSleep = System.currentTimeMillis()
          Thread.sleep(timeout.toMillis)
          def shouldTerminate = lastUsed.get() < beforeSleep
          // Double-checked locking.
          if (shouldTerminate)
            PythonLanguageDetector.this.synchronized {
              if (shouldTerminate) {
                scribe.info(s"Terminating Python language detection process after $timeout idle")
                $.destroyForcibly()
                break
              }
            }
        }
      },
    ).<|(_.setDaemon(true)).<|(_.setName("PythonLanguageDetector monitor")).start()
    $
  }
}

private object PythonLanguageDetector {
  /**
   * Creating a new detector initializes a new Python process, so this can be expensive, relatively
   * speaking (~200 msec).
   * @param timeout
   *   How long the process should be kept alive for if there is no interaction.
   */
  def create(timeout: Duration): PythonLanguageDetector = new PythonLanguageDetector(timeout)

  private val Encoding = StandardCharsets.UTF_8
}
