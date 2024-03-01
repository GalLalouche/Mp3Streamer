package models

import com.google.common.collect.BiMap
import models.MusicFinder.DirectoryName

import common.io.{DirectoryRef, FileRef, IODirectory, IOFile, IOSystem, JsonMapFile}
import common.rich.collections.RichTraversableOnce.richTraversableOnce

/** Can be extended to override its values in tests */
class IOMusicFinder extends MusicFinder {
  final override type S = IOSystem
  override val baseDir = IODirectory("G:/media/music")
  protected override def genresWithSubGenres: Seq[String] = Vector("Rock", "Metal")
  override def flatGenres: Seq[String] = Vector("New Age", "Jazz", "Musicals", "Classical")

  override val extensions = Set("mp3", "flac")
  override def parseSong(f: FileRef) = IOSong.read(f.asInstanceOf[IOFile].file)
  override def getOptionalSongsInDir(d: DirectoryRef): Seq[OptionalSong] =
    getSongFilesInDir(d).map(SongTagParser optionalSong _.file)
  protected override lazy val invalidDirectoryNames: BiMap[DirectoryName, ArtistName] =
    JsonMapFile.readJsonMap(getClass.getResourceAsStream("directory_renames.json")).toBiMap
}

/**
 * The actual locations, as opposed to mocked ones. This is used by scripts as well as the real
 * controllers.
 */
object IOMusicFinder extends IOMusicFinder
