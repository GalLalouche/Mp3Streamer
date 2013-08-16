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
package models

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
import akka.actor.ActorDSL
import akka.actor.PoisonPill
import common.Debug
import common.Directory
import akka.actor.ReceiveTimeout

class DirectoryWatcher(val mf: MusicFinder, listener: () => Unit) extends Actor with Debug {
	require(mf != null)
	require(listener != null)
	val watchService = Paths.get(mf.dir.path).getFileSystem.newWatchService
	
	private val keys = new HashMap[WatchKey, Directory]
	private val trace = false
	
	override def preStart {
		mf.genreDirs.foreach(registerAll)
	}
	/**
	  * Register a particular file or directory to be watched
	  */
	def register(dir: Directory): Unit = {
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
	  * Makes it easier to walk a file tree
	  */
	implicit def makeDirVisitor(f: (Path) => Unit) = new SimpleFileVisitor[Path] {
		override def preVisitDirectory(p: Path, attrs: BasicFileAttributes) = {
			f(p)
			FileVisitResult.CONTINUE
		}
	}

	/**
	  *  Recursively register directories
	  */
	def registerAll(dir: Directory): Unit = {
		register(dir)
		dir.dirs.foreach(registerAll(_))
	}

	/**
	  * The main directory watching thread
	  */
	import scala.concurrent.duration._
	context.setReceiveTimeout(0 milliseconds)
	override def receive = {
		case ReceiveTimeout  => waitForChange
		case PoisonPill => context.stop(self)
	}
	
	override def postStop {
		loggers.CompositeLogger.debug("DirectoryWatcher has left the building")
	}

	private def waitForChange = {
		val key = watchService.take()
		val dir = keys(key)
		key.pollEvents().asScala.filterNot(_.kind == StandardWatchEventKinds.OVERFLOW).
			filter(_.kind == StandardWatchEventKinds.ENTRY_CREATE).foreach(event => {
				val child = Paths.get(dir.path).resolve(event.context().asInstanceOf[Path])
				if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS))
					registerAll(Directory(child.toAbsolutePath.toFile));
			})

		if (key.reset() == false) {
			keys.remove(key);
			if (keys.isEmpty) break // nothing left watch
		}
		listener()
	}
}