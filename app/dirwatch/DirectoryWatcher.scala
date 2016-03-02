/**
  * dirwatcher.scala
  *
  * Uses the Java 7 WatchEvent filesystem API from within Scala.
  * Adapted from:
  *  http://download.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
  *
  * @author Chris Eberle <eberle1080@gmail.com>
  * @version 0.1
  */
package dirwatch

import java.io.File
import java.nio.file._

import akka.actor._
import common.Debug
import common.rich.path.Directory
import dirwatch.DirectoryWatcher._

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable.{HashMap, HashSet}
import scala.concurrent.duration.DurationInt

//TODO this shit doesn't justify its own package
class DirectoryWatcher(listener: ActorRef, val dirs: Traversable[Directory]) extends Actor with Debug {
	def this(listener: ActorRef, dir: Directory, dirs: Directory*) = this(listener, dir :: (dirs.toList))
	require(listener != null)
	require(dirs != null)

	private lazy val watchService = dirs.head.getFileSystem.newWatchService

	private val keys = HashMap[WatchKey, Path]()
	private val folders = HashSet[Path]()
	private val trace = false

	override def preStart {
		dirs.foreach(registerAll)
		listener ! Started
	}

	private def register(dir: Path) {
		trySleep(maxTries = 5, sleepTime = 1000) {
			val key = dir.register(watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
				//				StandardWatchEventKinds.ENTRY_MODIFY, // ignoring modify because it's called on deletes, and delete is called on modifies :\
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

	/**
	  *  Recursively register directories
	  */
	private def registerAll(dir: Directory) {
		register(dir)
		dir.dirs.foreach(registerAll(_))
	}

	context.setReceiveTimeout(0 milliseconds)
	override def receive = {
		case PoisonPill => context.stop(self)
		case _ => waitForChange
	}

	private def handleEvent(p: Path, e: java.nio.file.WatchEvent[_]) = {
		val resolvedPath = p.resolve(e.context().asInstanceOf[Path])
		val file = resolvedPath.toFile
		e.kind match {
			case StandardWatchEventKinds.ENTRY_DELETE if folders(p) => { folders -= p; DirectoryDeleted(file) }
			case StandardWatchEventKinds.ENTRY_CREATE if file.isDirectory => DirectoryCreated(Directory(resolvedPath.toFile))
			case _ => OtherChange
		}
	}

	private def waitForChange = {
		val key = watchService.take()
		val publishingPath = keys(key)
		val event = key.pollEvents().asScala;
		event
			.filter(_.kind == StandardWatchEventKinds.ENTRY_CREATE)
			.map(e => publishingPath.resolve(e.context.asInstanceOf[Path]))
			.filter(Files.isDirectory(_, LinkOption.NOFOLLOW_LINKS))
			.foreach(c => registerAll(Directory(c.toAbsolutePath.toFile)))

		event.foreach(listener ! handleEvent(publishingPath, _))

		if (key.reset() == false)
			keys.remove(key);
	}

	override def postStop {
		loggers.CompositeLogger.debug("DirectoryWatcher has left the building")
	}
}

object DirectoryWatcher {
	implicit def dirToPath(d: Directory): Path = Paths.get(d.path)
	
	case class DirectoryCreated(d: Directory)
	case class DirectoryDeleted(f: File) // can't be directory because it doesn't exist anymore, D'oh
	case class FileDeleted(f: File)
	case class FileCreated(f: File)
	case object OtherChange
	case object Started // for testing - letting the user of the actor know that the class has started
}
