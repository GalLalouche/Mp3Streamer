package backend.new_albums.filler

import backend.recon.{Album, Artist, ReconcilableFactory}
import com.google.inject.Inject
import musicfinder.{MusicFinder, SongDirectoryParser}

import scala.concurrent.ExecutionContext

import common.TimedLogger
import common.rich.RichFuture.richFuture
import common.rich.primitives.RichOption.richOption

/**
 * By RealTime, we mean that nothing is actually cached, rather, the artist and albums are fetched
 * from disk everytime these methods are called. Not very good performance, but ensure results are
 * up to date, which makes it better for a running server (or at least easier, since we don't have
 * to invalidate caches).
 */
private class RealTimeExistingAlbums @Inject() (
    reconcilableFactory: ReconcilableFactory,
    mf: MusicFinder,
    dirParser: SongDirectoryParser,
    timed: TimedLogger,
    manualAlbumsFinder: ManualAlbumsFinder,
    ec: ExecutionContext,
) extends ExistingAlbums {
  private implicit val iec: ExecutionContext = ec
  override def artists: Iterable[Artist] = timed("Fetching artists (lazy)", scribe.info(_)) {
    reconcilableFactory.artistDirectories.flatMap { artistDir =>
      val albumDirs = artistDir.dirs
      // "Standard" artists have albums prefixed with release year.
      if (albumDirs.exists(_.name.take(4).forall(_.isDigit)))
        Vector(reconcilableFactory.toArtist(artistDir))
      else // Non-standard artists, e.g., DT Sides, don't.
        albumDirs.map(dir =>
          Artist(dirParser(dir).headOption.getOrThrow(s"Problem with $artistDir").artistName),
        )
    }.toVector
  }

  override def albums: Artist => Set[Album] = artist =>
    getAlbums(artist)
      .orElse(manualAlbumsFinder.!(artist).get)
      .getOrThrow(s"Could not find albums for artist $artist")

  private def getAlbums(artist: Artist): Option[Set[Album]] =
    mf.findArtistDir(artist).map(_.dirs.map(reconcilableFactory.toAlbum(_).get).toSet)
}
