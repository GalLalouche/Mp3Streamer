package musicfinder

import rx.lang.scala.Observable

import common.io.{DirectoryRef, FileRef}

/** Provides access to music on the filesystem, using some base directory and a list of a genres. */
trait MusicFiles {
  /** The base directory under which all music is stored. Split into genres. */
  def baseDir: DirectoryRef
  /** Dirs which host artists/albums at the top level, e.g., musicals. */
  def flatGenres: Seq[String]
  /** For example, Metal is split into Death Metal, Black Metal, etc. */
  def genreDirsWithSubGenres: Seq[DirectoryRef]
  def artistDirs: Observable[DirectoryRef]
  def albumDirs: Observable[DirectoryRef]
  /** All song files found in the music base directory. */
  def getSongFiles: Observable[FileRef]
  final def albumDirs(startingFrom: Iterable[DirectoryRef]): Observable[DirectoryRef] =
    albumDirs(Observable.from(startingFrom))
  def albumDirs(startingFrom: Observable[DirectoryRef]): Observable[DirectoryRef]
}
