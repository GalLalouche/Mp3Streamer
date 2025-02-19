package musicfinder

import com.google.common.collect.BiMap
import models.IOSong
import musicfinder.MusicFinder.DirectoryName

import common.io.{BaseDirectory, FileRef, IODirectory, IOFile, IOSystem, JsonMapFile}
import common.rich.collections.RichTraversableOnce.richTraversableOnce

/** Can be extended to override its values in scripts. */
class IOMusicFinder(@BaseDirectory override val baseDir: IODirectory) extends MusicFinder {
  def this() = this(IOMusicFinderModule.BaseDir)
  final override type S = IOSystem
  protected override def genresWithSubGenres: Seq[String] = Vector("Rock", "Metal")
  override def flatGenres: Seq[String] = Vector("New Age", "Jazz", "Musicals", "Classical")

  override val extensions = IOMusicFinder.extensions
  override val unsupportedExtensions = IOMusicFinder.unsupportedExtensions
  override def parseSong(f: FileRef) = IOSong.read(f.asInstanceOf[IOFile].file)
  protected override def invalidDirectoryNames: BiMap[DirectoryName, backend.recon.Artist] =
    IOMusicFinder.invalidDirectoryNames
}

object IOMusicFinder {
  private lazy val invalidDirectoryNames: BiMap[DirectoryName, backend.recon.Artist] =
    JsonMapFile
      .readJsonMap(getClass.getResourceAsStream("directory_renames.json"))
      .mapValues(backend.recon.Artist(_))
      .toBiMap
  private val extensions = Set("mp3", "flac")
  private val unsupportedExtensions = Set("ape", "wma", "mp4", "wav", "aiff", "aac", "ogg", "m4a")
}
