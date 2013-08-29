package common.path

import java.io.File

class Path protected (val p: File) {
	require(p != null)
	require(p exists, p + " doesn't exist")
	
	val path = p.getAbsolutePath
	val name = p.getName

	def /(s: String): Path = {
		val f = new File(path + "/" + s)
		if (f isDirectory) Directory(f) else new RichFile(f)
	}
	def \(s: String): File = new File(path + "\\" + s)
	def \(): File = this \ ""

	def / = new Directory(p)

	override def toString = name

	override def hashCode = p.hashCode
	override def equals(o: Any): Boolean = if (o.isInstanceOf[Path]) return p.equals(o.asInstanceOf[Path].p) else false
	
	lazy val parent: Directory = { // this has to be lazy, to avoid computing entire path to root in construction
		if (p.getParentFile() == null) 
			throw new UnsupportedOperationException("File: " + p + " has no parent")
		Directory(p.getParentFile())
	}
}

object Path {
	implicit def richPath(f: File) = if (f isDirectory) new Directory(f.getAbsoluteFile) else new RichFile(f)
	implicit def poorPath(p: Path): File = p.p
}