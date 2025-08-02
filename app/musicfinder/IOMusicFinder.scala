package musicfinder

import com.google.inject.Inject
import models.ArtistName
import musicfinder.MusicFinder.DirectoryName

import common.io.{BaseDirectory, IODirectory, IOSystem}

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
  protected override def normalizeArtistName(name: ArtistName): DirectoryName =
    artistNameNormalizer(name)
}

object IOMusicFinder {
  private val extensions = Set("mp3", "flac")
  private val unsupportedExtensions = Set("ape", "wma", "mp4", "wav", "aiff", "aac", "ogg", "m4a")
}
