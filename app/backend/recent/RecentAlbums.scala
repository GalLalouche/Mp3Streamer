package backend.recent

import java.nio.file.attribute.BasicFileAttributes
import java.time.{Clock, Instant}
import java.time.temporal.ChronoUnit

import com.google.inject.Inject
import models.{AlbumDir, AlbumDirFactory, SongTagParser}
import musicfinder.{MusicFiles, SongFileFinder}

import scala.collection.mutable

import cats.syntax.apply.catsSyntaxApplyOps

import common.path.ref.{DirectoryRef, FileRef}
import common.rich.RichT._
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
  import RecentAlbums.fileTimeNewestOrdering
  override def since(since: Instant): Seq[AlbumDir] =
    mf.albumDirsWithAttributes(Some(since))
      .toVectorBlocking // TODO we can seq and sort at the same time.
      .sorted
      .map(_._1 |> makeAlbum)
  def sinceDays(d: Int): Seq[AlbumDir] = since(_.minus(d, ChronoUnit.DAYS))
  def sinceMonths(m: Int): Seq[AlbumDir] = since(_.minus(m, ChronoUnit.MONTHS))
  private def since(f: Instant => Instant): Seq[AlbumDir] =
    since(f(clock.instant().truncatedTo(ChronoUnit.DAYS)))
  private def isDoubleAlbum(albumDir: AlbumDir): Boolean = {
    val songs = finder.getSongFilesInDir(albumDir.dir).sortBy(_.name)
    def discNumber(s: FileRef) = songTagParser(s).discNumber
    discNumber(songs.head).map2(discNumber(songs.last))(_ != _).getOrElse(false)
  }
  private def sortedDirs(): Iterator[AlbumDir] =
    mf.albumDirsWithAttributes
      .buildBlocking(mutable.ArrayBuilder.make)
      // TODO this could be sped up even further with a heap, since it's O(n) for building!
      .sortInPlace
      .iterator
      .map(_._1)
      .map(makeAlbum)
  // recent doesn't care about songs.
  private def makeAlbum(dir: DirectoryRef) = albumFactory.fromDirWithoutSongs(dir)
}

object RecentAlbums {
  private implicit val fileTimeNewestOrdering: Ordering[(DirectoryRef, BasicFileAttributes)] =
    Ordering
      .by[(DirectoryRef, BasicFileAttributes), Instant](_._2.lastModifiedTime.toInstant)
      .reverse
}
