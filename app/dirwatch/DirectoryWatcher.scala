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
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.concurrent.duration.DurationInt
import scala.util.control.Breaks.break

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.actorRef2Scala
import common.Debug
import common.path.Directory
import common.path.Path.poorPath
import DirectoryWatcher._

class DirectoryWatcher(listener: ActorRef, val dirs: Option[List[Directory]]) extends Actor with Debug {
	def this(listener: ActorRef) = this(listener, None)
	def this(listener: ActorRef, dir: Directory) = this(listener, Some(List(dir)))
	def this(listener: ActorRef, dirs: List[Directory]) = this(listener, Some(dirs))
	require(listener != null)
	require(dirs != null)

	private lazy val watchService = Paths.get(dirs.getOrElse(List(Directory("C:/")))(0).path).getFileSystem.newWatchService

	private val keys = HashMap[WatchKey, Directory]()
	private val folders = HashSet[String]()
	private val trace = false

	override def preStart {
		if (dirs.isDefined)
			dirs.get.foreach(registerAll)
		listener ! Started
	}

	/**
	  * Register a particular file or directory to be watched
	  */
	private def register(dir: Directory) {
		import resource._
		trySleep(5) {
			val key = Paths.get(dir.path).register(watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
//				StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE)
			if (trace) {
				val prev = keys.getOrElse(key, null)
				if (prev == null) {
					println("register: " + dir)
				} else {
					if (!dir.equals(prev)) {
						println("update: " + prev + " -> " + dir)
					}
				}
			}

			folders += dir.getAbsolutePath
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

	/**
	  * The main directory watching thread
	  */
	context.setReceiveTimeout(0 milliseconds)
	override def receive = {
		case PoisonPill => context.stop(self)
		case _ => waitForChange
	}

	private def handleEvent(p: Path, e: java.nio.file.WatchEvent[_]) = {
		val path = p.resolve(e.context().asInstanceOf[Path])
		val file = path.toFile
		val isDirectory = Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
		e.kind match {
			case StandardWatchEventKinds.ENTRY_DELETE if (folders(file.getAbsolutePath)) =>  {folders -= file.getAbsolutePath; DirectoryDeleted(file)}
			case StandardWatchEventKinds.ENTRY_CREATE if (isDirectory) => DirectoryCreated(Directory(path))
			case _ => OtherChange
		}
	}

	private def waitForChange = {
		val key = watchService.take()
		val dir = keys(key)
		val event = key.pollEvents().asScala;
		event.filterNot(_.kind == StandardWatchEventKinds.OVERFLOW).
			filter(_.kind == StandardWatchEventKinds.ENTRY_CREATE).foreach(event => {
				val child = Paths.get(dir.path).resolve(event.context().asInstanceOf[Path])
				if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
					val dir = Directory(child.toAbsolutePath.toFile)
					registerAll(dir);
				}
			});

		event.foreach(e => listener ! handleEvent(Paths.get(dir.path), e))

		if (key.reset() == false) {
			keys.remove(key);
			if (keys.isEmpty) break // nothing left watch
		}
	}

	override def postStop {
		loggers.CompositeLogger.debug("DirectoryWatcher has left the building")
	}
}

object DirectoryWatcher {
	case class DirectoryCreated(d: Directory)
	case class DirectoryDeleted(f: File) // can't be directory because it doesn't exist anymore, D'oh
	case class FileDeleted(f: File)
	case class FileCreated(f: File)
	case object OtherChange
	case object Started // for testing - letting the user of the actor know that the class has started
}