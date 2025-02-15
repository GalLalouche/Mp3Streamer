package musicfinder

import com.google.common.collect.BiMap
import models.{IOSong, OptionalSong, SongTagParser}
import musicfinder.MusicFinder.DirectoryName

import common.ds.Types.ViewSeq
import common.io.{BaseDirectory, DirectoryRef, FileRef, IODirectory, IOFile, IOSystem, JsonMapFile}
import common.rich.collections.RichTraversableOnce.richTraversableOnce

/** Can be extended to override its values in tests */
class IOMusicFinder(@BaseDirectory override val baseDir: IODirectory) extends MusicFinder {
  def this() = this(IOMusicFinderModule.BaseDir)
  final override type S = IOSystem
  protected override def genresWithSubGenres: Seq[String] = Vector("Rock", "Metal")
  override def flatGenres: Seq[String] = Vector("New Age", "Jazz", "Musicals", "Classical")

  override val extensions = IOMusicFinder.extensions
  override val unsupportedExtensions = IOMusicFinder.unsupportedExtensions
  override def parseSong(f: FileRef) = IOSong.read(f.asInstanceOf[IOFile].file)
  override def getOptionalSongsInDir(d: DirectoryRef): ViewSeq[OptionalSong] =
    getSongFilesInDir(d).view.map(SongTagParser optionalSong _.file)
  protected override def invalidDirectoryNames: BiMap[DirectoryName, backend.recon.Artist] =
    IOMusicFinder.invalidDirectoryNames
}

/**
 * The actual locations, as opposed to mocked ones. This is used by scripts as well as the real
 * controllers.
 */
object IOMusicFinder {
  private lazy val invalidDirectoryNames: BiMap[DirectoryName, backend.recon.Artist] =
    JsonMapFile
      .readJsonMap(getClass.getResourceAsStream("directory_renames.json"))
      .mapValues(backend.recon.Artist(_))
      .toBiMap
  private val extensions = Set("mp3", "flac")
  private val unsupportedExtensions = Set("ape", "wma", "mp4", "wav", "aiff", "aac", "ogg", "m4a")
}
