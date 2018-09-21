package backend.recent

import java.time.ZoneOffset

import javax.inject.Inject
import models.{Album, AlbumFactory, MusicFinder}

private class RecentAlbums @Inject()(mf: MusicFinder, albumFactory: AlbumFactory) {
  def apply(amount: Int): Seq[Album] =
    mf.genreDirs
        .flatMap(_.deepDirs)
        .filter(mf.getSongFilesInDir(_).nonEmpty)
        .sortBy(_.lastModified)(Ordering.by(-_.toEpochSecond(ZoneOffset.UTC)))
        .take(amount)
        .map(albumFactory.fromDir)
        .map(Album.songs set Nil) // recent doesn't care about songs
}
