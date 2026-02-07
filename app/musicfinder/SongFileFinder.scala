package musicfinder

import common.path.ref.{DirectoryRef, FileRef}

/** Looks up song files in a given directory. */
abstract class SongFileFinder {
  val extensions: Set[String] = Set("mp3", "flac")
  def getSongFilesInDir(d: DirectoryRef): Iterator[FileRef] =
    d.files.filter(_.extensionIsAnyOf(extensions))
  def hasSongFiles(dir: DirectoryRef): Boolean = dir.containsFileWithExtension(extensions)
  def matchesExtension(f: FileRef): Boolean = extensions.exists(f.hasExtension)
}
