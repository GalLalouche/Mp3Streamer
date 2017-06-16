package backend.logging

import java.time.LocalDateTime
import java.util.concurrent.{Semaphore, TimeUnit, TimeoutException}

import backend.configs.{NewThreatExecutionContext, TestConfiguration}
import common.io.FileRef
import common.rich.collections.RichTraversableOnce._
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FreeSpec, ShouldMatchers}

/** A special kind of file that can block reads and writes. */
private[this] class BlockFileRef(val f: FileRef) extends FileRef {
  override type F = f.F
  private val lock = new Semaphore(0)
  private val changeLock = new Semaphore(0)
  def waitForChange(): Unit = {
    if (!changeLock.tryAcquire(1, TimeUnit.SECONDS))
      throw new TimeoutException()
  }
  def release(): Unit = lock.release()
  def block(): Unit = lock.drainPermits()
  override def bytes: Array[Byte] = {
    lock.acquire()
    val $ = f.bytes
    lock.release()
    $
  }
  override def write(s: String) = {
    lock.acquire()
    val $ = f.write(s)
    lock.release()
    changeLock.release()
    $
  }
  override def write(bs: Array[Byte]) = {
    lock.acquire()
    val $ = f.write(bs)
    lock.release()
    changeLock.release()
    $
  }
  override def appendLine(line: String) = {
    lock.acquire()
    val $ = f.appendLine(line)
    lock.release()
    changeLock.release()
    $
  }
  override def readAll: String = {
    lock.acquire()
    val $ = f.readAll
    lock.release()
    $
  }
  override def path: String = {
    lock.acquire()
    val $ = f.path
    lock.release()
    $
  }
  override def name: String = {
    lock.acquire()
    val $ = f.name
    lock.release()
    $
  }
  override def lastModified: LocalDateTime = {
    lock.acquire()
    val $ = f.lastModified
    lock.release()
    $
  }
}
class FileLoggerTest extends FreeSpec with ShouldMatchers with TimeLimitedTests {
  override val timeLimit = Span.apply(1, Seconds)
  private implicit val c = new TestConfiguration
  private val file: FileRef = c.rootDirectory.addFile("foobar")
  private val $ = new FileLogger(file)

  "writing" - {
    "foo" in {
      $ info "foobar"
      file.lines.single should endWith("foobar")
    }
    "blocking" in {
      val blockingFile = new BlockFileRef(file)
      val $ = new FileLogger(blockingFile)(NewThreatExecutionContext)
      $.info("foobar")
      blockingFile.release()
      blockingFile.waitForChange()
      file.lines.single should endWith("foobar")
    }
  }
}
