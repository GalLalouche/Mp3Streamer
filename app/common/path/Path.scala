package common.path

import java.io.File

class Path protected (val p: File) {
	require(p != null)
	require(p exists, p + " doesn't exist")
	protected def getPath = p.getAbsolutePath
	lazy val path = getPath
	val name = p.getName

	def /(s: String): Path = {
		val f = new File(path + "/" + s)
		if (f isDirectory) Directory(f) else new RichFile(f)
	}
	def \(s: String) = new File(path + "\\" + s)

	def / = new Directory(p)
	def \ = new File(path + "\\")
	override def toString = name

	lazy val extension = {
		val i = p.getName.lastIndexOf('.')
		if (i == -1) "" else p.getName.substring(i + 1).toLowerCase
	}

	override def hashCode = p.hashCode
	override def equals(o: Any): Boolean = if (o.isInstanceOf[Path]) return p.equals(o.asInstanceOf[Path].p) else false
	lazy val parent: Directory = {
		if (p.getParentFile() == null) 
			throw new UnsupportedOperationException("File: " + p + " has no parent")
		Directory(p.getParentFile())
	}
}

object Path {
	implicit def richPath(f: File) = if (f isDirectory) new Directory(f.getAbsoluteFile) else new RichFile(f)
	implicit def poorPath(p: Path): File = p.p
}