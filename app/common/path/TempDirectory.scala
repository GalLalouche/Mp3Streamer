package common.path

import java.io.File

/**
 * A directory that deletes itself on exit.
 * Used in testing
 */
class TempDirectory(f: File) extends Directory(f: File) {
	require(f != null)
	require(f isDirectory)
	f.deleteOnExit
	
	override def addSubDir(name: String) = {
		TempDirectory(super.addSubDir(name).dir)
	}
	
	override def addFile(name: String) = {
		val $ = super.addFile(name)
		$.deleteOnExit
		$
	}
	
	override def cloneDir = {
		val $ = super.cloneDir
		$.deleteOnExit
		$
	}
}

object TempDirectory {
	def apply(f: File) = new TempDirectory(f)
}