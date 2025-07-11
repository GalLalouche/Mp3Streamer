package genre

import backend.recon.Artist
import com.google.inject.{Inject, Singleton}
import genre.Genre.Musicals
import musicfinder.MusicFinder

import common.TimedLogger
import common.io.DirectoryRef
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichOption.richOption

@Singleton private class StringGenreFinder @Inject() (mf: MusicFinder, timedLogger: TimedLogger) {
  private lazy val artistDirs: Map[Artist, mf.S#D] =
    timedLogger("Fetching artistDirs")(mf.artistDirs).mapBy(Artist apply _.name)

  def forArtist(artist: backend.recon.Artist): Option[StringGenre] =
    artistDirs.get(artist).map(forDir)

  def forDir(dir: DirectoryRef): StringGenre = {
    require(
      dir.path.startsWith(mf.baseDir.path),
      s"<$dir> is not a subdirectory of <${mf.baseDir}>",
    )
    val relativeDir = dir.relativize(mf.baseDir.asInstanceOf[dir.S#D])
    if (relativeDir.isEmpty)
      // Single album musicals, e.g., Grease
      return StringGenre.Flat(dir.name.ensuring(_ == Musicals.name))
    val parentsFromBaseDir = relativeDir.reverse
    val topDirName = parentsFromBaseDir.head.name
    if (mf.flatGenres contains topDirName)
      StringGenre.Flat(topDirName)
    else
      StringGenre.Nested(
        topDirName,
        parentsFromBaseDir.tail.headOption
          .getOrThrow(s"<$dir> is a top genre, not an actual artist directory")
          .name,
      )
  }
}
