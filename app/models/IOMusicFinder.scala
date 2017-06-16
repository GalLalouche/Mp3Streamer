package models

import common.io.IODirectory
import common.rich.path.Directory

/** Can be extended to override its values in tests */
class IOMusicFinder extends MusicFinder {
	override type D = IODirectory
	override val dir = IODirectory("d:/media/music")
	override protected val subDirNames = List("Rock", "New Age", "Classical", "Metal", "Jazz")
	override val extensions = Set("mp3", "flac")
}

/** The actual locations, as opposed to mocked ones. This is used by scripts as well as the real controllers. */
object IOMusicFinder extends IOMusicFinder
