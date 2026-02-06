package musicfinder

import java.nio.file.attribute.BasicFileAttributes

import rx.lang.scala.Observable

import common.path.ref.{DirectoryRef, RefSystem}
import common.rich.collections.RichIterable

private abstract class MusicFilesImpl[S <: RefSystem.Aux[S]](
    override val baseDir: S#D,
    sff: SongFileFinder,
) extends MusicFiles { self =>
  /**
   * Dirs with an extra level of sub-genre nesting, e.g., metal which has death and black metal
   * inside it.
   */
  protected def genresWithSubGenres: Seq[String]
  /** Dirs which host artists/albums at the top level, e.g., musicals. */
  override def flatGenres: Seq[String]
  private def getDir(name: String): S#D = baseDir.getDir(name).get
  private def allGenres = genresWithSubGenres ++ flatGenres
  override def genreDirsWithSubGenres: Seq[S#D] = genresWithSubGenres.map(getDir)
  override def artistDirs: Observable[S#D] = {
    def getDirs(xs: Seq[String]): Observable[S#D] =
      Observable.from(xs).map(getDir(_).dirs).flatMapIterable(i => RichIterable.from(() => i))

    getDirs(genresWithSubGenres).flatMap(d =>
      Observable.from(RichIterable.from(() => d.dirs)),
    ) ++ getDirs(flatGenres)
  }

  protected def genreDirs: Seq[S#D] = allGenres.sorted.map(getDir)
  override def albumDirsWithAttributes: Observable[(S#D, BasicFileAttributes)] =
    albumDirsWithAttributes(Observable.from(genreDirs))
  override def albumDirsWithAttributes(
      startingFrom: Observable[DirectoryRef],
  ): Observable[(S#D, BasicFileAttributes)] =
    startingFrom
      .flatMap(_.deepDirsObservable)
      .asInstanceOf[Observable[(S#D, BasicFileAttributes)]]
      // Because some albums have, e.g., cover subdirectories
      .filter(sff hasSongFiles _._1)
  override def getSongFiles: Observable[S#F] =
    Observable
      .from(genreDirs)
      .flatMap(_.deepFilesObservable)
      .map(_._1)
      .filter(sff.matchesExtension)
}
