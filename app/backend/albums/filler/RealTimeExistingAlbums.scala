package backend.albums.filler

import backend.logging.LoggingLevel
import backend.recon.{Album, Artist, ReconcilableFactory}
import javax.inject.{Inject, Singleton}
import models.MusicFinder

import common.rich.primitives.RichOption.richOption
import common.TimedLogger

@Singleton private class RealTimeExistingAlbums @Inject() (
    reconcilableFactory: ReconcilableFactory,
    mf: MusicFinder,
    timed: TimedLogger,
) extends ExistingAlbums {
  override def artists: Iterable[Artist] = timed("Fetching artists (lazy)", LoggingLevel.Info) {
    reconcilableFactory.artistDirectories.flatMap { artistDir =>
      val dirs = artistDir.dirs
      // "Standard" artists have albums prefixed with release year.
      if (dirs.exists(_.name.take(4).forall(_.isDigit)))
        Vector(reconcilableFactory.dirNameToArtist(artistDir.name))
      else // Non-standard artists, e.g., DT Sides, don't
        dirs.map(dir =>
          Artist(
            mf.parseSong(
              mf
                .getSongFilesInDir(dir)
                .headOption
                .getOrThrow(s"Problem with $artistDir"),
            ).artistName,
          ),
        )
    }.toVector
  }
  override def albums: Artist => Set[Album] = artist =>
    mf.findArtistDir(artist.name)
      .get
      .dirs
      .map(reconcilableFactory.toAlbum(_).get)
      .toSet
}
