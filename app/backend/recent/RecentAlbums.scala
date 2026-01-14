package backend.recent

import java.time.{Clock, LocalDate, LocalDateTime}

import com.google.inject.Inject
import models.{AlbumDir, AlbumDirFactory, SongTagParser}
import musicfinder.MusicFinder
import rx.lang.scala.Observable

import scala.Ordering.Implicits._

import cats.syntax.apply.catsSyntaxApplyOps

import common.TopKBuilder
import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.RichTime
import common.rich.RichTime.RichClock
import common.rich.collections.RichIterator.richIterator
import common.rx.RichObservable.richObservable

private class RecentAlbums @Inject() (
    mf: MusicFinder,
    songTagParser: SongTagParser,
    albumFactory: AlbumDirFactory,
    clock: Clock,
) extends LastAlbumProvider {
  // recent doesn't care about songs. TODO fromDirWithoutSongs
  private def makeAlbum(dir: DirectoryRef) = albumFactory.fromDir(dir).copy(songs = Nil)
  private def go(amount: Int)(dirs: Observable[DirectoryRef]) =
    dirs
      .buildBlocking(TopKBuilder[DirectoryRef](amount)(Ordering.by(_.lastModified)))
      .map(makeAlbum)
  def all(amount: Int): Seq[AlbumDir] = mf.albumDirs |> go(amount)
  def double(amount: Int): Seq[AlbumDir] = mf.albumDirs.filter(isDoubleAlbum) |> go(amount)
  override def since(lastDuration: LocalDateTime): Seq[AlbumDir] =
    mf.albumDirs
      .filter(_.lastModified >= lastDuration)
      .toVectorBlocking // TODO we can seq and sort at the same time.
      .sortBy(_.lastModified)(RichTime.OrderingLocalDateTime.reverse)
      .map(makeAlbum)
  def sinceDays(d: Int): Seq[AlbumDir] = since(_.minusDays(d))
  def sinceMonths(m: Int): Seq[AlbumDir] = since(_.minusMonths(m))
  private def since(f: LocalDate => LocalDate): Seq[AlbumDir] =
    since(f(clock.getLocalDate).atStartOfDay)
  private def isDoubleAlbum(dir: DirectoryRef): Boolean = {
    val songs = mf.getSongFilesInDir(dir).sortBy(_.name)
    def discNumber(s: FileRef) = songTagParser(s).discNumber
    discNumber(songs.head).map2(discNumber(songs.last))(_ != _).getOrElse(false)
  }
}
