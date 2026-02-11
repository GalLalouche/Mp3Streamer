package musicfinder

import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime

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
  final def albumDirsWithAttributes: DirsWithAttributes = albumDirsWithAttributes(None)
  /** It's faster to filter by date than checking for the existence of song files. */
  def albumDirsWithAttributes(since: Option[LocalDateTime]): DirsWithAttributes
  /** It's faster to filter by date than checking for the existence of song files. */
  def albumDirsWithAttributes(
      startingFrom: Observable[DirectoryRef],
      since: Option[LocalDateTime] = None,
  ): DirsWithAttributes

  type DirsWithAttributes = Observable[(DirectoryRef, BasicFileAttributes)]
}
