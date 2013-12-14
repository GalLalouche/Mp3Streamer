package models

import common.path.Path._
import common.path.Directory
import java.io.File
import common.path.RichFile
/**
 * Handles cover art
 */
object Poster {
	private def getCoverArt(dir: Directory): File = {
		val f = dir.files.find(_.name.toLowerCase.matches("folder.(jpg)|(png)"))
		f.getOrElse(getCoverArt(dir.parent))
	}
	def getCoverArt(s: Song): File = {
		getCoverArt(s.file.parent)
	}
}