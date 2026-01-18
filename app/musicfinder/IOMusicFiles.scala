package musicfinder

import rx.lang.scala.Observable

import scala.collection.View

import common.io.{DirectoryRef, IODirectory, IOFile}

trait IOMusicFiles extends MusicFiles {
  def withSongFileFinder(sff: IOSongFileFinder): IOMusicFiles
  override def baseDir: IODirectory
  override def genreDirsWithSubGenres: Seq[IODirectory]
  override def artistDirs: View[IODirectory]
  override def getSongFiles: Observable[IOFile]
  override def albumDirs: Observable[IODirectory]
  override def albumDirs(startingFrom: Observable[DirectoryRef]): Observable[IODirectory]
}
