package common.path

import java.io.File

/**
  * Helper class for Directory methods
  */
class Directory(val dir: File) extends Path(dir) {
	require(dir != null)
	require(dir isDirectory)

	/**
	  * Adds a new file under the directory
	  * @param name The file's name
	  * @return the file created
	  */
	def addFile(name: String) = {
		val $ = new File(dir, name);
		$.createNewFile()
		$
	}
	/**
	  * Adds a new sub-directory under this directory
	  * @param name The directory's name
	  * @return the directory created
	  */
	def addSubDir(name: String) = {
		val $ = new File(dir, name);
		$.mkdir
		Directory($)
	}

	/**
	  * @return all direct subdirs of this dir
	  */
	def dirs = {
		Option(dir.listFiles).getOrElse(Array()).toVector.filter(_.isDirectory).map(Directory(_))
	}
	/**
	  * All direct files of this dir
	  */
	def files = Option(dir.listFiles).getOrElse(Array()).toVector.filterNot(_.isDirectory)
	/**
	  * Deletes all files and directories in this dir recursively including itself
	  */
	def deleteAll {
		System.gc()
		def deleteAll(d: Directory) {
			System.gc()
			d.dirs.foreach(deleteAll)
			d.files.foreach(x => { if (x.exists && x.delete == false) println("could not delete: " + x) })
			if (d.dir.exists && d.dir.delete == false) {
				System.gc()
				println("could not delete: " + d.dir)
			}
		}
		deleteAll(this)
	}
	/**
	  * Deletes all files and directories in this dir recursively <b>not</b> including itself
	  */
	def clear {
		deleteAll
		dir.mkdir
	}
	/**
	  * @return all files that are not dirs nested inside this dir (in any given depth)
	  */
	final def deepFiles: Seq[File] = {
		files ++ dirs.flatMap(_.deepFiles)
	}
	final def deepDirs: Seq[Directory] = {
		dirs ++ dirs.flatMap(_.deepDirs)
	}
	def deepPaths: Seq[Path] = {
		files.map(new RichFile(_)) ++ dirs ++  dirs.flatMap(_.deepPaths)
	}
}

object Directory {
	def apply(f: File): Directory = new Directory(f)
	def apply(s: String): Directory = Directory(new File(s))
	def apply(p: Path): Directory = Directory(p.p)
}