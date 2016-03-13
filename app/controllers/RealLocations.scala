package controllers

import common.io.IODirectory
import common.rich.path.Directory
import models.MusicFinder

/** Can be extended to override its values in tests */
class RealLocations extends MusicFinder {
	override val dir = IODirectory("d:/media/music")
	override val subDirs = List("Rock", "New Age", "Classical", "Metal")
	override val extensions = List("mp3", "flac")
	override val genreDirs: Seq[IODirectory] = super.genreDirs.asInstanceOf[Seq[IODirectory]]
	def getSongFilePathsInDir(d: Directory): Seq[String] = super.getSongFilePathsInDir(new IODirectory(d))
}

/** The actual locations, as opposed to mocked ones. This is used by scripts as well as the real controllers. */
object RealLocations extends RealLocations
