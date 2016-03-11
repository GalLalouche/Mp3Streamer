package controllers

import common.io.IODirectory
import models.MusicFinder

/** The actual locations, as opposed to mocked ones. This is used by scripts as well as the real controllers. */
class RealLocations extends MusicFinder {
	override val dir = IODirectory("d:/media/music")
	override val subDirs = List("Rock", "New Age", "Classical", "Metal")
	override val extensions = List("mp3", "flac")
	override val genreDirs: Seq[IODirectory] = super.genreDirs.asInstanceOf[Seq[IODirectory]]
}

object RealLocations extends RealLocations
