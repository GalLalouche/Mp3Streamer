package models

import common.io.{FileRef, IODirectory, IOFile, IOSystem}

/** Can be extended to override its values in tests */
class IOMusicFinder extends MusicFinder {
  override final type S = IOSystem
  override val dir = IODirectory("d:/media/music")
  override protected val subDirNames: Seq[String] = Vector("Rock", "New Age", "Classical", "Metal", "Jazz")
  override val extensions = Set("mp3", "flac")
  override def parseSong(f: FileRef) = IOSong.read(f.asInstanceOf[IOFile].file)
}

/** The actual locations, as opposed to mocked ones. This is used by scripts as well as the real controllers. */
object IOMusicFinder extends IOMusicFinder
