package backend.recent

import java.time.{Clock, LocalDate}

import com.google.inject.Inject
import models.{AlbumDir, AlbumDirFactory, SongTagParser}
import musicfinder.MusicFinder

import scala.Ordering.Implicits._
import scala.collection.View
import scala.concurrent.Future

import cats.syntax.apply.catsSyntaxApplyOps

import common.concurrency.SimpleTypedActor
import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.RichTime._
import common.rich.collections.RichTraversableOnce.richTraversableOnce

private class RecentAlbums @Inject() (
    mf: MusicFinder,
    songTagParser: SongTagParser,
    albumFactory: AlbumDirFactory,
    clock: Clock,
) extends LastAlbumProvider {
  // recent doesn't care about songs.
  private def makeAlbum(dir: DirectoryRef) = albumFactory.fromDir(dir).copy(songs = Nil)
  private def go(amount: Int)(dirs: View[DirectoryRef]) =
    dirs.topK(amount)(Ordering.by(_.lastModified)).map(makeAlbum)
  private val lastFetcher =
    SimpleTypedActor.unique[Unit, AlbumDir]("last fetcher", all(1).head.const)
  override def last: Future[AlbumDir] = lastFetcher ! ()
  def all(amount: Int): Seq[AlbumDir] = mf.albumDirs |> go(amount)
  def double(amount: Int): Seq[AlbumDir] = mf.albumDirs.filter(isDoubleAlbum) |> go(amount)
  private def since(f: LocalDate => LocalDate): Seq[AlbumDir] = {
    val lastDuration = f(clock.getLocalDate)
    mf.albumDirs
      .filter(_.lastModified.toLocalDate >= lastDuration)
      .toVector
      .sortBy(_.lastModified)(OrderingLocalDateTime.reverse)
      .map(makeAlbum)
  }
  def sinceDays(d: Int): Seq[AlbumDir] = since(_.minusDays(d))
  def sinceMonths(m: Int): Seq[AlbumDir] = since(_.minusMonths(m))
  private def isDoubleAlbum(dir: DirectoryRef): Boolean = {
    val songs = mf.getSongFilesInDir(dir).sortBy(_.name)
    def discNumber(s: FileRef) = songTagParser(s).discNumber
    discNumber(songs.head).map2(discNumber(songs.last))(_ != _).getOrElse(false)
  }
}
