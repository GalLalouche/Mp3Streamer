package backend.new_albums.filler

import backend.new_albums.DirectoryDiscovery
import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.recon.ReconcilableFactory.AlbumParseError.SinglesDirectory
import com.google.inject.Inject
import musicfinder.ArtistDirResult.{MultipleArtists, NoMatch, SingleArtist}
import musicfinder.ArtistDirsIndex

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
    directoryDiscovery: DirectoryDiscovery,
    artistDirsIndex: ArtistDirsIndex,
    timed: TimedLogger,
    manualAlbumsFinder: ManualAlbumsFinder,
    ec: ExecutionContext,
) extends ExistingAlbums {
  private implicit val iec: ExecutionContext = ec
  override def artists: Iterable[Artist] = timed("Fetching artists (lazy)", scribe.info(_)) {
    directoryDiscovery.artistDirectories.flatMap { artistDir =>
      lazy val albumDir = artistDir.dirs.headOption.getOrThrow(s"Problem with $artistDir")
      artistDirsIndex.forDir(artistDir) match {
        case SingleArtist(artist) => Vector(artist)
        case MultipleArtists(artists) => artists
        case NoMatch =>
          scribe.warn(s"Artist directory <${artistDir.path}> does not appear in index")
          Vector(reconcilableFactory.extractArtistFromAlbumDir(albumDir))
      }
    }.toVector
  }

  override def albums: Artist => Set[Album] = artist =>
    getAlbums(artist)
      .orElse(manualAlbumsFinder.!(artist).get)
      .getOrThrow(s"Could not find albums for artist $artist")

  private def getAlbums(artist: Artist): Option[Set[Album]] =
    artistDirsIndex
      .forArtist(artist)
      .map(
        _.dirs
          .flatMap(reconcilableFactory.toAlbum(_) match {
            case Left(SinglesDirectory) => None
            case Right(a) => Some(a)
          })
          .toSet,
      )
}
