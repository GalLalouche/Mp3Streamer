package backend.recent

import javax.inject.Inject
import models.{Album, AlbumFactory, MusicFinder}

import scalaz.std.option.optionInstance
import scalaz.syntax.apply.^
import common.rich.func.MoreSeqInstances._
import common.rich.func.ToMoreFoldableOps._

import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.RichTime.OrderingLocalDateTime

private class RecentAlbums @Inject()(mf: MusicFinder, albumFactory: AlbumFactory) {
  private def go(amount: Int)(dirs: Seq[DirectoryRef]) = dirs
      .topK(amount)(Ordering.by(_.lastModified))
      .map(albumFactory.fromDir(_).copy(songs = Nil)) // recent doesn't care about songs.
  def all(amount: Int): Seq[Album] = mf.albumDirs |> go(amount)
  def double(amount: Int): Seq[Album] = mf.albumDirs.filter(isDoubleAlbum) |> go(amount)
  private def isDoubleAlbum(dir: DirectoryRef): Boolean = {
    val songs = mf.getSongFilesInDir(dir)
    def discNumber(s: FileRef) = mf.parseSong(s).discNumber
    ^(discNumber(songs.head), discNumber(songs.last))(_ != _) getOrElse false
  }
}
