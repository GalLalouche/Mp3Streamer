package musicfinder

import common.io.{DirectoryRef, FileRef}

/** Looks up song files in a given directory. */
abstract class SongFileFinder {
  val extensions: Set[String] = Set("mp3", "flac")
  def getSongFilesInDir(d: DirectoryRef): Iterator[FileRef] =
    d.files.filter(extensions contains _.extension)
  def hasSongFiles(dir: DirectoryRef): Boolean = dir.files.exists(extensions contains _.extension)
}
