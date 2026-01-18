package musicfinder

import rx.lang.scala.Observable

import scala.collection.View

import common.io.{DirectoryRef, FileRef}

/** Provides access to music on the filesystem, using some base directory and a list of a genres. */
trait MusicFiles {
  def baseDir: DirectoryRef
  /** Dirs which host artists/albums at the top level, e.g., musicals. */
  def flatGenres: Seq[String]
  def genreDirsWithSubGenres: Seq[DirectoryRef]
  def artistDirs: View[DirectoryRef]
  /** All song files found in the music base directory. */
  def getSongFiles: Observable[FileRef]
  def albumDirs: Observable[DirectoryRef]
  final def albumDirs(startingFrom: Iterable[DirectoryRef]): Observable[DirectoryRef] =
    albumDirs(Observable.from(startingFrom))
  def albumDirs(startingFrom: Observable[DirectoryRef]): Observable[DirectoryRef]
}
