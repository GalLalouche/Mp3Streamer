package common.path

import java.io.File

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
}

object TempDirectory {
	def apply(f: File) = new TempDirectory(f)
}