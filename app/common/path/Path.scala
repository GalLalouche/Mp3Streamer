package common.path

import java.io.File

class Path protected (val p: File) {
	require(p != null)
	require(p exists)
	protected def getPath = p.getAbsolutePath
	lazy val path = getPath
	val name = p.getName

	def /(s: String): Path = {
		val f = new File(path + "/" + s)
		if (f isDirectory) Directory(f) else new RichFile(f)
	}

	def / = new Directory(p)

	override def toString = name

	lazy val extension = {
		val i = p.getName.lastIndexOf('.')
		p.getName.substring(i + 1).toLowerCase
	}
}

object Path {
	implicit def richPath(f: File) = if (f isDirectory) new Directory(f.getAbsoluteFile) else new RichFile(f)
}