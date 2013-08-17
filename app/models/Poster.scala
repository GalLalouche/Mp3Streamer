package models

import common.path.Path._
/**
 * Handles cover art
 */
object Poster {
	def getCoverArt(s: Song) = {
		s.file.parent.files.filter(_.name.matches("[fF]older.jpg"))(0)
	}
}