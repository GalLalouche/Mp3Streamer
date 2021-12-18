package backend.albums.filler

import backend.logging.LoggingLevel
import backend.recon.{Album, Artist}
import javax.inject.{Inject, Singleton}
import models.MusicFinder

import common.rich.primitives.RichOption.richOption
import common.TimedLogger

@Singleton private class LazyExistingAlbums @Inject()(mf: MusicFinder, timed: TimedLogger)
    extends ExistingAlbums {
  override def artists: Iterable[Artist] = timed("Fetching artists (lazy)", LoggingLevel.Info) {
    ExistingAlbums.artistDirectories(mf)
        .flatMap {artistDir =>
          val dirs = artistDir.dirs
          // "Standard" artists have albums prefixed with release year.
          if (dirs.exists(_.name.take(4).forall(_.isDigit)))
            Vector(ExistingAlbums dirNameToArtist artistDir.name)
          else // Non-standard artists, e.g., DT Sides, don't
            dirs.map(dir => Artist(mf.parseSong(mf.getSongFilesInDir(dir).headOption.getOrThrow(s"Problem with $artistDir")).artistName))
        }.toVector
  }
  override def albums: Artist => Set[Album] = artist => mf.findArtistDir(artist.name).get
      .dirs
      .map(ExistingAlbums.toAlbum(mf))
      .toSet
}
