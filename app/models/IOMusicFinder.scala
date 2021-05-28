package models

import common.io.{DirectoryRef, FileRef, IODirectory, IOFile, IOSystem}

/** Can be extended to override its values in tests */
class IOMusicFinder extends MusicFinder {
  override final type S = IOSystem
  override val dir = IODirectory("G:/media/music")
  override protected def genresWithSubGenres: Seq[String] = Vector("Rock", "Metal")
  override protected def flatGenres: Seq[String] = Vector("New Age", "Jazz", "Musicals", "Classical")

  override val extensions = Set("mp3", "flac")
  override def parseSong(f: FileRef) = IOSong.read(f.asInstanceOf[IOFile].file)
  override def getOptionalSongsInDir(d: DirectoryRef): Seq[OptionalSong] =
    getSongFilesInDir(d).map(SongTagParser optionalSong _.file)
}

/** The actual locations, as opposed to mocked ones. This is used by scripts as well as the real controllers. */
object IOMusicFinder extends IOMusicFinder
