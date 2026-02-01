package musicfinder

import common.path.ref.{DirectoryRef, FileRef}

/** Looks up song files in a given directory. */
abstract class SongFileFinder {
  val extensions: Set[String] = Set("mp3", "flac")
  def getSongFilesInDir(d: DirectoryRef): Iterator[FileRef] =
    d.files.filter(extensions exists _.hasExtension)
  def hasSongFiles(dir: DirectoryRef): Boolean = dir.containsFileWithExtension(extensions)
}
