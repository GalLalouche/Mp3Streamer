package common.path

import java.io.File

class RichFile(val f: File) extends Path(f) {
}

object RichFile {
	implicit def poorFile(f: RichFile) = f.f
}

