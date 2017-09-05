package backend.logging

import java.time.LocalDateTime
import java.util.concurrent.{Semaphore, TimeUnit, TimeoutException}

import backend.configs.TestConfiguration
import common.io.MemoryFile
import common.rich.collections.RichTraversableOnce._
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.{Second, Span}
import org.scalatest.{FreeSpec, ShouldMatchers}

import scala.concurrent.ExecutionContext

/** A special kind of file that blocks on reads and writes. */
private[this] class BlockFileRef(val f: MemoryFile) extends MemoryFile(f.parent, f.name) {
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
  override def lastModified: LocalDateTime = {
    lock.acquire()
    val $ = f.lastModified
    lock.release()
    $
  }
}
class FileLoggerTest extends FreeSpec with ShouldMatchers with TimeLimitedTests {
  override val timeLimit = Span.apply(1, Second)
  private implicit val c = new TestConfiguration
  private val file = c.rootDirectory.addFile("foobar")
  private val $ = new FileLogger(file)

  "writing" - {
    "should write to file" in {
      $ info "foobar"
      file.lines.single should endWith("foobar")
    }
    "should run in its own thread" in {
      // this is tested by using a file that blocks on writing. If the writing isn't done in its
      // own thread, this method will timeout since it won't reach the semaphore release command.
      val blockingFile = new BlockFileRef(file)
      val $ = new FileLogger(blockingFile)(new ExecutionContext {
        override def execute(runnable: Runnable): Unit = new Thread(runnable).start()
        override def reportFailure(cause: Throwable): Unit = {
          throw cause
        }
      })
      $.info("foobar")
      blockingFile.release()
      blockingFile.waitForChange()
      file.lines.single should endWith("foobar")
    }
  }
}
