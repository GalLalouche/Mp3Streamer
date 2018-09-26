package backend.recent

import common.rich.func.{MoreSeqInstances, ToMoreFoldableOps}
import javax.inject.Inject
import backend.RichTime.OrderingLocalDateTime
import models.{Album, AlbumFactory, MusicFinder}

private class RecentAlbums @Inject()(mf: MusicFinder, albumFactory: AlbumFactory)
    extends ToMoreFoldableOps with MoreSeqInstances {
  def apply(amount: Int): Seq[Album] = mf.genreDirs
      .flatMap(_.deepDirs)
      .filter(mf.getSongFilesInDir(_).nonEmpty)
      .topK(amount)(Ordering.by(_.lastModified))
      .map(albumFactory.fromDir(_).copy(songs = Nil)) // recent doesn't care about songs
}
