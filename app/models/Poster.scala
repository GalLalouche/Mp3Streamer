package models

import common.io.{DirectoryRef, FileRef}

object Poster {
	private def getCoverArt(dir: DirectoryRef): FileRef = {
		val f = dir.files.find(_.name.toLowerCase.matches("folder.(jpg)|(png)"))
		f.getOrElse(getCoverArt(dir.parent))
	}
	def getCoverArt(s: Song): FileRef = getCoverArt(s.file.parent)
}
