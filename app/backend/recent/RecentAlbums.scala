package backend.recent

import java.time.{Clock, LocalDate, LocalDateTime}

import com.google.inject.Inject
import models.{AlbumDir, AlbumDirFactory, SongTagParser}
import musicfinder.{MusicFiles, SongFileFinder}
import rx.lang.scala.Observable

import scala.math.Ordering.Implicits.infixOrderingOps

import cats.syntax.apply.catsSyntaxApplyOps

import common.TopKBuilder
import common.path.ref.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.RichTime
import common.rich.RichTime.{RichClock, RichFileTime}
import common.rich.collections.RichIterator.richIterator
import common.rx.RichObservable.richObservable

private class RecentAlbums @Inject() (
    finder: SongFileFinder,
    mf: MusicFiles,
    songTagParser: SongTagParser,
    albumFactory: AlbumDirFactory,
    clock: Clock,
) extends LastAlbumProvider {
  def all(amount: Int): Seq[AlbumDir] = go(amount, mf.albumDirs)
  def double(amount: Int): Seq[AlbumDir] = go(amount, mf.albumDirs.filter(isDoubleAlbum))
  override def since(lastDuration: LocalDateTime): Seq[AlbumDir] =
    mf.albumDirsWithAttributes
      .filter(_._2.lastModifiedTime.toLocalDateTime >= lastDuration)
      .toVectorBlocking // TODO we can seq and sort at the same time.
      .sortBy(_._2.lastModifiedTime.toLocalDateTime)(RichTime.OrderingLocalDateTime.reverse)
      .map(_._1 |> makeAlbum)
  def sinceDays(d: Int): Seq[AlbumDir] = since(_.minusDays(d))
  def sinceMonths(m: Int): Seq[AlbumDir] = since(_.minusMonths(m))
  private def since(f: LocalDate => LocalDate): Seq[AlbumDir] =
    since(f(clock.getLocalDate).atStartOfDay)
  private def isDoubleAlbum(dir: DirectoryRef): Boolean = {
    val songs = finder.getSongFilesInDir(dir).sortBy(_.name)
    def discNumber(s: FileRef) = songTagParser(s).discNumber
    discNumber(songs.head).map2(discNumber(songs.last))(_ != _).getOrElse(false)
  }
  private def go(amount: Int, dirs: Observable[DirectoryRef]) =
    dirs
      .buildBlocking(TopKBuilder[DirectoryRef](amount)(Ordering.by(_.lastModifiedTime)))
      .map(makeAlbum)
  // recent doesn't care about songs.
  private def makeAlbum(dir: DirectoryRef) = albumFactory.fromDirWithoutSongs(dir)
}
