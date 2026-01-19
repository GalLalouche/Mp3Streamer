package backend.new_albums.filler

import backend.new_albums.DirectoryDiscovery
import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.recon.ReconcilableFactory.AlbumParseError.SinglesDirectory
import com.google.inject.Inject
import musicfinder.ArtistDirResult.{MultipleArtists, NoMatch, SingleArtist}
import musicfinder.ArtistDirsIndex
import rx.lang.scala.Observable

import common.rich.RichFuture.richFutureBlocking
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
    manualAlbumsFinder: ManualAlbumsFinder,
) extends ExistingAlbums {
  override def artists: Observable[Artist] =
    directoryDiscovery.artistDirectories.flatMapIterable { artistDir =>
      lazy val albumDir = artistDir.dirs.nextOption().getOrThrow(s"Problem with $artistDir")
      artistDirsIndex.forDir(artistDir) match {
        case SingleArtist(artist) => Vector(artist)
        case MultipleArtists(artists) => artists
        case NoMatch =>
          scribe.warn(s"Artist directory <${artistDir.path}> does not appear in index")
          Vector(reconcilableFactory.extractArtistFromAlbumDir(albumDir))
      }
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
