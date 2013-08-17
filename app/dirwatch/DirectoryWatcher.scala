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
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.nio.file.attribute.BasicFileAttributes
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable.HashMap
import scala.util.control.Breaks.break
import akka.actor.Actor
import akka.actor.PoisonPill
import akka.actor.ReceiveTimeout
import common.Debug
import common.Directory
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorDSL
import akka.actor.ActorSystem

class DirectoryWatcher(listener: ActorRef, val dir: Option[Directory]) extends Actor with Debug {
	def this(listener: ActorRef) = this(listener, None)
	def this(listener: ActorRef, dir: Directory) = this(listener, Some(dir))
	require(listener != null)
	require(dir != null)

	private lazy val watchService = Paths.get(dir.getOrElse(Directory("C:/")).path).getFileSystem.newWatchService

	private val keys = HashMap[WatchKey, Directory]()
	private val trace = false

	override def preStart {
		if (dir.isDefined)
			registerAll(dir.get)
	}

	/**
	  * Register a particular file or directory to be watched
	  */
	private def register(dir: Directory) {
		import resource._
		trySleep(5) {
			val key = Paths.get(dir.path).register(watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY,
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

	private def waitForChange = {
		val key = watchService.take()
		val dir = keys(key)
		key.pollEvents().asScala.filterNot(_.kind == StandardWatchEventKinds.OVERFLOW).
			filter(_.kind == StandardWatchEventKinds.ENTRY_CREATE).foreach(event => {
				val child = Paths.get(dir.path).resolve(event.context().asInstanceOf[Path])
				if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
					listener ! DirectoryWatcher.FolderCreated
					registerAll(Directory(child.toAbsolutePath.toFile));
				}
			})

		if (key.reset() == false) {
			keys.remove(key);
			if (keys.isEmpty) break // nothing left watch
		}

		listener ! DirectoryWatcher.OtherChange
	}

	override def postStop {
		loggers.CompositeLogger.debug("DirectoryWatcher has left the building")
	}
}

object DirectoryWatcher {
	case object FolderCreated
	case object OtherChange
}