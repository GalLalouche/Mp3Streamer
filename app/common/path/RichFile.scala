package common.path

import java.io.File

class RichFile(val f: File) extends Path(f) {
	val parent = Directory(f.getParentFile())
}

object RichFile {
	implicit def poorFile(f: RichFile) = f.f
}

