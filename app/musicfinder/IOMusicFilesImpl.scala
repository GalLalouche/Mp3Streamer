package musicfinder

import com.google.inject.Inject

import common.io.{BaseDirectory, IODirectory, IOSystem}

/** Can be extended to override its values in scripts. */
private class IOMusicFilesImpl @Inject() (
    @BaseDirectory _baseDir: IODirectory,
    artistNameNormalizer: ArtistNameNormalizer,
    sff: IOSongFileFinder,
) extends MusicFilesImpl[IOSystem](_baseDir, sff)
    with IOMusicFiles {
  override def withSongFileFinder(sff: IOSongFileFinder): IOMusicFiles =
    new IOMusicFilesImpl(baseDir, artistNameNormalizer, sff)
  protected override def genresWithSubGenres: Seq[String] = Vector("Rock", "Metal")
  override def flatGenres: Seq[String] = Vector("New Age", "Jazz", "Musicals", "Classical")
}
