/**
 * dirwatcher.scala
 *
 * Uses the Java 7 WatchEvent filesystem API from within Scala.
 * Adapted from:
 * http://download.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
 * @original_author Chris Eberle <eberle1080@gmail.com>
 * @version 0.1
 */
package common.concurrency

import java.io.File
import java.nio.file._

import common.Debug
import common.concurrency.DirectoryWatcher._
import common.io.IODirectory
import common.rich.RichT._
import common.rich.path.Directory
import models.MusicFinder
import rx.lang.scala.{Observable, Observer, Subscription}

import scala.annotation.tailrec
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable

private class DirectoryWatcher(listener: Observer[DirectoryEvent], val dirs: Traversable[Directory]) extends Debug {
  require(listener != null)
  require(dirs != null)
  def start() {
    dirs foreach registerAll
    listener onNext Started
    waitForChange()
  }

  private lazy val watchService = dirs.head.toPath.getFileSystem.newWatchService

  private val keys = mutable.HashMap[WatchKey, Path]()
  private val folders = mutable.HashSet[Path]()
  private val trace = false

  /** Recursively register directories */
  private def registerAll(dir: Directory) {
    register(dir.toPath)
    dir.dirs foreach registerAll
  }
  /** Register a single directory */
  private def register(dir: Path) {
    trySleep(maxTries = 5, sleepTime = 1000) {
      val key = dir.register(watchService,
        StandardWatchEventKinds.ENTRY_CREATE,
        // ignoring ENTRY_MODIFY because it's called on deletes, and delete is called on modifies :\
        StandardWatchEventKinds.ENTRY_DELETE)
      if (trace) {
        val prev = keys.getOrElse(key, null)
        if (prev == null)
          println("register: " + dir)
        else if (!dir.equals(prev))
          println("update: " + prev + " -> " + dir)
      }

      folders += dir
      keys(key) = dir
    }
  }

  private def handleEvent(p: Path, e: java.nio.file.WatchEvent[_]): DirectoryEvent = {
    val resolvedPath = p.resolve(e.context().asInstanceOf[Path])
    val file = resolvedPath.toFile
    e.kind match {
      case StandardWatchEventKinds.ENTRY_DELETE if folders(p) => folders -= p; DirectoryDeleted(file)
      case StandardWatchEventKinds.ENTRY_CREATE if file.isDirectory => DirectoryCreated(Directory(resolvedPath.toFile))
      case _ => OtherChange
    }
  }

  @tailrec
  private def waitForChange() {
    val key = watchService.take()
    val publishingPath = keys(key)
    val event = key.pollEvents().asScala
    event // register all new directories
        .toStream
        .filter(_.kind == StandardWatchEventKinds.ENTRY_CREATE)
        .map(_.context.asInstanceOf[Path])
        .map(publishingPath.resolve)
        .filter(Files.isDirectory(_, LinkOption.NOFOLLOW_LINKS))
        .map(_.toAbsolutePath.toFile)
        .map(Directory.apply)
        .foreach(registerAll)

    event // notify all new directories
        .map(handleEvent(publishingPath, _))
        .foreach(listener.onNext)

    if (key.reset() == false)
      keys remove key
    waitForChange()
  }
}

object DirectoryWatcher {
  private implicit def dirToPath(d: Directory): Path = Paths.get(d.path)

  sealed abstract class DirectoryEvent
  case class DirectoryCreated(d: Directory) extends DirectoryEvent
  case class DirectoryDeleted(f: File) extends DirectoryEvent
  // can't be directory since it doesn't exist anymore, D'oh
  case class FileDeleted(f: File) extends DirectoryEvent
  case class FileCreated(f: File) extends DirectoryEvent
  case object OtherChange extends DirectoryEvent
  // for testing - letting the user of the actor know that the class has started
  case object Started extends DirectoryEvent

  def apply(ref: MusicFinder): Observable[DirectoryEvent] = ref.genreDirs.map(_.asInstanceOf[IODirectory].dir) |> apply
  def apply(dirs: Traversable[Directory]): Observable[DirectoryEvent] = Observable(o => {
    new SingleThreadedJobQueue() {
      override val name = "DirectoryWatcher"
    }.apply {
      val dw = new DirectoryWatcher(o, dirs)
      dw.start()
    }
    Subscription.apply(???)
  })
}
