package models

import common.rich.path.Directory
import common.rich.path.RichFile._
import java.io.File
import common.Debug

trait MusicFinder extends Debug {
	val dir: Directory
	val subDirs: List[String]
	val extensions: List[String]

	lazy val genreDirs = subDirs.sorted.map(x => Directory(dir / x))
	def getAlbums: Iterator[Album] = genreDirs
		.iterator
		.flatMap(_.deepDirs)
		.collect {
			case d => d.files.filter(f => extensions.contains(f.extension))
		}.filter(_.nonEmpty)
		.map(files => Album(Song(files.head)))
	def getSongFilePaths: IndexedSeq[String] = {
		(genreDirs.flatMap(_.files) ++ (genreDirs.flatMap(_.dirs).par.flatMap(_.deepFiles)))
			.filter(x => extensions.contains(x.extension))
			.map(_.path)
			.toVector
	}
	def getSongIterator: Iterator[Song] = {
		timed("finding files") { (genreDirs.flatMap(_.files) ++ (genreDirs.flatMap(_.dirs).par.flatMap(_.deepFiles))) }
			.iterator
			.filter(x => extensions.contains(x.extension))
			.map(_.f)
			.map(Song.apply)
	}
}
