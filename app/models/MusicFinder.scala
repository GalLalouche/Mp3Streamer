package models

import common.Directory
import common.Path._
import java.io.File
import scala.collection.parallel.ForkJoinTaskSupport


trait MusicFinder {
	val dir: Directory
	val subDirs: List[String]
	val extensions: List[String]
	
	lazy val genreDirs = subDirs.map(x => Directory(dir.path + "/" + x))
	def getSongs: Seq[String] = {
		(genreDirs.flatMap(_.files) ++ (genreDirs.flatMap(_.dirs).par.flatMap(_.deepFiles)))
				.filter(x => extensions.contains(x.extension)).map(_.path).toVector
	}

}
