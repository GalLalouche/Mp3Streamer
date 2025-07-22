package musicfinder

import com.google.common.collect.BiMap
import com.google.inject.Inject
import models.ArtistName
import musicfinder.MusicFinder.DirectoryName

import common.io.{BaseDirectory, IODirectory, IOSystem, JsonMapFile}
import common.rich.collections.RichTraversableOnce.richTraversableOnce

/** Can be extended to override its values in scripts. */
class IOMusicFinder @Inject() (
    @BaseDirectory override val baseDir: IODirectory,
    artistNameNormalizer: ArtistNameNormalizer,
) extends MusicFinder {
  protected def this(artistNameNormalizer: ArtistNameNormalizer) =
    this(IOMusicFinderModule.BaseDir, artistNameNormalizer)
  final override type S = IOSystem
  protected override def genresWithSubGenres: Seq[String] = Vector("Rock", "Metal")
  override def flatGenres: Seq[String] = Vector("New Age", "Jazz", "Musicals", "Classical")

  override val extensions = IOMusicFinder.extensions
  override val unsupportedExtensions = IOMusicFinder.unsupportedExtensions
  protected override def invalidDirectoryNames: BiMap[DirectoryName, backend.recon.Artist] =
    IOMusicFinder.invalidDirectoryNames
  protected override def normalizeArtistName(name: ArtistName): DirectoryName =
    artistNameNormalizer(name)
}

object IOMusicFinder {
  lazy val invalidDirectoryNames: BiMap[DirectoryName, backend.recon.Artist] =
    JsonMapFile
      .readJsonMap(getClass.getResourceAsStream("directory_renames.json"))
      .mapValues(backend.recon.Artist(_))
      .toBiMap
  private val extensions = Set("mp3", "flac")
  private val unsupportedExtensions = Set("ape", "wma", "mp4", "wav", "aiff", "aac", "ogg", "m4a")
}
