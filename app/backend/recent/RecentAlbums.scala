package backend.recent

import java.time.{Clock, LocalDate, LocalDateTime}

import com.google.inject.Inject
import models.{AlbumDir, AlbumDirFactory, SongTagParser}
import musicfinder.{MusicFiles, SongFileFinder}

import scala.collection.mutable
import scala.math.Ordering.Implicits.infixOrderingOps

import cats.syntax.apply.catsSyntaxApplyOps

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
  def all(amount: Int): Seq[AlbumDir] = sortedDirs().take(amount).toVector
  def double(amount: Int): Seq[AlbumDir] = sortedDirs().filter(isDoubleAlbum).take(amount).toVector
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
  private def isDoubleAlbum(albumDir: AlbumDir): Boolean = {
    val songs = finder.getSongFilesInDir(albumDir.dir).sortBy(_.name)
    def discNumber(s: FileRef) = songTagParser(s).discNumber
    discNumber(songs.head).map2(discNumber(songs.last))(_ != _).getOrElse(false)
  }
  private def sortedDirs(): Iterator[AlbumDir] =
    mf.albumDirsWithAttributes
      .buildBlocking(mutable.ArrayBuilder.make)
      .sortInPlaceBy(_._2.lastModifiedTime.toLocalDateTime)
      .map(_._1)
      .iterator
      // TODO this could be sped up even further with a heap, since it's O(n) for building!
      .map(makeAlbum)
  // recent doesn't care about songs.
  private def makeAlbum(dir: DirectoryRef) = albumFactory.fromDirWithoutSongs(dir)
}
