package musicfinder

import models.ArtistName
import musicfinder.MusicFinder.DirectoryName
import rx.lang.scala.Observable

import scala.collection.View

import common.io.{DirectoryRef, RefSystem}
import common.rich.collections.RichIterable

trait MusicFinder { self =>
  type S <: RefSystem { type S = self.S }
  def baseDir: S#D
  def extensions: Set[String]
  /** Known file extensions which aren't supported for various reasons, e.g., monkey 🙉. */
  def unsupportedExtensions: Set[String]

  /**
   * Dirs with an extra level of sub-genre nesting, e.g., metal which has death and black metal
   * inside it.
   */
  protected def genresWithSubGenres: Seq[String]
  /** Dirs which host artists/albums at the top level, e.g., musicals. */
  def flatGenres: Seq[String]
  private def getDir(name: String) = baseDir.getDir(name).get
  private def allGenres = genresWithSubGenres ++ flatGenres
  def genreDirsWithSubGenres: Seq[S#D] = genresWithSubGenres.map(getDir)
  def artistDirs: View[S#D] = {
    def getDirs(xs: Seq[String]): View[S#D] = xs.view.map(getDir).flatMap(_.dirs)
    getDirs(genresWithSubGenres).flatMap(_.dirs) ++ getDirs(flatGenres)
  }

  protected def normalizeArtistName(name: ArtistName): DirectoryName

  def albumDirs: DirView = albumDirs(startingFrom = genreDirs)
  protected def genreDirs: Seq[S#D] = allGenres.sorted.map(getDir)
  def albumDirs(startingFrom: Iterable[S#D]): DirView = Observable
    .from(startingFrom)
    .flatMap(_.deepDirsObservable.asInstanceOf[Observable[S#D]])
    // Because some albums have, e.g., cover subdirectories
    .filter(_.files.exists(f => extensions.contains(f.extension)))
  def getSongFiles: Observable[S#F] =
    albumDirs.flatMapIterable(d => RichIterable.from(() => getSongFilesInDir(d)))
  def getSongFilesInDir(d: DirectoryRef): Iterator[S#F] =
    d.asInstanceOf[S#D].files.filter(f => extensions.contains(f.extension))

  final type DirView = Observable[S#D]
}

object MusicFinder {
  type DirectoryName = String
}
