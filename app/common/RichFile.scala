package common

import java.io.File

class RichFile(val f: File) extends Path(f) {
	require (f exists)
	
	val parent = Directory(f.getParentFile())
}

object RichFile {
	implicit def poorFile(f: RichFile) = f.f
}

