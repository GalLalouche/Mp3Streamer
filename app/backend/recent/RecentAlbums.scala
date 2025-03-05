package backend.recent

import java.time.{Clock, LocalDate}
import com.google.inject.Inject

import models.{AlbumDir, AlbumDirFactory}
import musicfinder.MusicFinder

import scala.Ordering.Implicits._

import common.rich.func.MoreSeqInstances._
import common.rich.func.ToMoreFoldableOps._
import scalaz.std.option.optionInstance
import scalaz.syntax.apply.^

import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.RichTime._

private class RecentAlbums @Inject() (
    mf: MusicFinder,
    albumFactory: AlbumDirFactory,
    clock: Clock,
) {
  // recent doesn't care about songs.
  private def makeAlbum(dir: DirectoryRef) = albumFactory.fromDir(dir).copy(songs = Nil)
  private def go(amount: Int)(dirs: Seq[DirectoryRef]) = dirs
    .topK(amount)(Ordering.by(_.lastModified))
    .map(makeAlbum)
  def all(amount: Int): Seq[AlbumDir] = mf.albumDirs |> go(amount)
  def double(amount: Int): Seq[AlbumDir] = mf.albumDirs.filter(isDoubleAlbum) |> go(amount)
  private def since(f: LocalDate => LocalDate): Seq[AlbumDir] = {
    val lastDuration = f(clock.getLocalDate)
    mf.albumDirs
      .filter(_.lastModified.toLocalDate >= lastDuration)
      .sortBy(_.lastModified)(OrderingLocalDateTime.reverse)
      .map(makeAlbum)
  }
  def sinceDays(d: Int): Seq[AlbumDir] = since(_.minusDays(d))
  def sinceMonths(m: Int): Seq[AlbumDir] = since(_.minusMonths(m))
  private def isDoubleAlbum(dir: DirectoryRef): Boolean = {
    val songs = mf.getSongFilesInDir(dir)
    def discNumber(s: FileRef) = mf.parseSong(s).discNumber
    ^(discNumber(songs.head), discNumber(songs.last))(_ != _).getOrElse(false)
  }
}
