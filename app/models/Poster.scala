package models

import java.io.File

import common.rich.path.Directory
import common.rich.path.RichPath._

object Poster {
	private def getCoverArt(dir: Directory): File = {
		val f = dir.files.find(_.name.toLowerCase.matches("folder.(jpg)|(png)"))
		f.getOrElse(getCoverArt(dir.parent))
	}
	def getCoverArt(s: Song): File = {
		getCoverArt(s.file.parent)
	}
}
