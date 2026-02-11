package musicfinder

import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime

import rx.lang.scala.Observable

import common.path.ref.DirectoryRef
import common.path.ref.io.{IODirectory, IOFile}

trait IOMusicFiles extends MusicFiles {
  def withSongFileFinder(sff: IOSongFileFinder): IOMusicFiles
  override def baseDir: IODirectory
  override def genreDirsWithSubGenres: Seq[IODirectory]
  override def artistDirs: Observable[IODirectory]
  override def getSongFiles: Observable[IOFile]
  override def albumDirs: Observable[IODirectory] =
    super.albumDirs.asInstanceOf[Observable[IODirectory]]
  override def albumDirs(startingFrom: Observable[DirectoryRef]): Observable[IODirectory] =
    super.albumDirs(startingFrom).asInstanceOf[Observable[IODirectory]]
  override def albumDirsWithAttributes(
      since: Option[LocalDateTime],
  ): Observable[(IODirectory, BasicFileAttributes)]
  override def albumDirsWithAttributes(
      startingFrom: Observable[DirectoryRef],
      since: Option[LocalDateTime] = None,
  ): Observable[(IODirectory, BasicFileAttributes)]
}
