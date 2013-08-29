package common.path

import java.io.File

class RichFile(val f: File) extends Path(f) {

	lazy val extension = {
		val i = p.getName.lastIndexOf('.')
		if (i == -1) "" else p.getName.substring(i + 1).toLowerCase
	}
}

object RichFile {
	implicit def poorFile(f: RichFile): File = f.f
	implicit def richFile(f: File): RichFile = new RichFile(f)

	def apply(f: File) = new RichFile(f)
}

