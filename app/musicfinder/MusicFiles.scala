package musicfinder

import java.nio.file.attribute.BasicFileAttributes

import rx.lang.scala.Observable

import common.path.ref.{DirectoryRef, FileRef}

/** Provides access to music on the filesystem, using some base directory and a list of a genres. */
trait MusicFiles {
  /** The base directory under which all music is stored. Split into genres. */
  def baseDir: DirectoryRef
  /** Dirs which host artists/albums at the top level, e.g., musicals. */
  def flatGenres: Seq[String]
  /** For example, Metal is split into Death Metal, Black Metal, etc. */
  def genreDirsWithSubGenres: Seq[DirectoryRef]
  def artistDirs: Observable[DirectoryRef]
  /** All song files found in the music base directory. */
  def getSongFiles: Observable[FileRef]
  final def albumDirs(startingFrom: Iterable[DirectoryRef]): Observable[DirectoryRef] =
    albumDirs(Observable.from(startingFrom))
  def albumDirs: Observable[DirectoryRef] = albumDirsWithAttributes.map(_._1)
  def albumDirs(startingFrom: Observable[DirectoryRef]): Observable[DirectoryRef] =
    albumDirsWithAttributes(startingFrom).map(_._1)
  def albumDirsWithAttributes: Observable[(DirectoryRef, BasicFileAttributes)]
  def albumDirsWithAttributes(
      startingFrom: Observable[DirectoryRef],
  ): Observable[(DirectoryRef, BasicFileAttributes)]
}
