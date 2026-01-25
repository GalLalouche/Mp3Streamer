package musicfinder

import common.io.{DirectoryRef, FileRef}

/** Looks up song files in a given directory. */
abstract class SongFileFinder {
  val extensions: Set[String] = Set("mp3", "flac")
  def getSongFilesInDir(d: DirectoryRef): Iterator[FileRef] =
    d.files.filter(extensions contains _.extension)
  // Could be useful to speed things up! https://gemini.google.com/app/dc80c8f105b7f367
  def hasSongFiles(dir: DirectoryRef): Boolean = dir.containsFileWithExtension(extensions)
}
