package models

import common.path.Path._
import common.path.Directory
import java.io.File
/**
 * Handles cover art
 */
object Poster {
	private def getCoverArt(dir: Directory): File = {
		val f = dir.files.find(_.name.matches("[fF]older.jpg"))
		f.getOrElse(getCoverArt(dir.parent))
	}
	def getCoverArt(s: Song): File = {
		getCoverArt(s.file.parent)
	}
}