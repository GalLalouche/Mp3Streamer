package models

import javax.inject.{Inject, Singleton}

import common.rich.collections.RichTraversableOnce._
import common.TimedLogger
import common.io.DirectoryRef
import common.rich.primitives.RichOption.richOption

@Singleton private class StringGenreFinder @Inject()(
    mf: MusicFinder, timedLogger: TimedLogger) {
  private lazy val artistDirs = timedLogger("Fetching artistDirs") {
    mf.artistDirs
  }.mapBy(_.name.toLowerCase)

  def forArtist(artist: backend.recon.Artist): Option[StringGenre] =
    artistDirs.get(artist.normalize).map(forDir)

  def forDir(dir: DirectoryRef): StringGenre = {
    require(dir.path startsWith mf.baseDir.path, s"<$dir> is not a subdirectory of <${mf.baseDir}>")
    val relativeDir = dir.relativize(mf.baseDir.asInstanceOf[dir.S#D])
    if (relativeDir.isEmpty)
      return StringGenre.Flat(dir.name.ensuring(_ == "Musicals")) // Single album musicals, e.g., Grease
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
