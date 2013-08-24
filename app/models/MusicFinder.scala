package models

import common.path.Directory
import common.path.Path.richPath


trait MusicFinder {
	val dir: Directory
	val subDirs: List[String]
	val extensions: List[String]
	
	lazy val genreDirs = subDirs.sorted.map(x => Directory(dir / x))
	def getSongs: IndexedSeq[String] = {
		(genreDirs.flatMap(_.files) ++ (genreDirs.flatMap(_.dirs).par.flatMap(_.deepFiles)))
				.filter(x => extensions.contains(x.extension)).map(_.path).toVector
	}

}
