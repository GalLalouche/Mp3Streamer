package backend.recent

import backend.RichTime.OrderingLocalDateTime
import javax.inject.Inject
import models.{Album, AlbumFactory, MusicFinder}

import common.rich.func.MoreSeqInstances._
import common.rich.func.ToMoreFoldableOps._

private class RecentAlbums @Inject()(mf: MusicFinder, albumFactory: AlbumFactory) {
  def apply(amount: Int): Seq[Album] = mf.genreDirs
      .flatMap(_.deepDirs)
      .filter(mf.getSongFilesInDir(_).nonEmpty)
      .topK(amount)(Ordering.by(_.lastModified))
      .map(albumFactory.fromDir(_).copy(songs = Nil)) // recent doesn't care about songs
}
