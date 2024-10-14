package backend.albums.filler

import javax.inject.Inject

import backend.recon.{Album, Artist, ReconcilableFactory}
import models.MusicFinder
import models.TypeAliases.ArtistName

import common.TimedLogger
import common.io.DirectoryRef
import common.rich.RichT.richT
import common.rich.primitives.RichOption.richOption

private class RealTimeExistingAlbums @Inject() (
    reconcilableFactory: ReconcilableFactory,
    mf: MusicFinder,
    timed: TimedLogger,
) extends ExistingAlbums {
  override def artists: Iterable[Artist] = timed("Fetching artists (lazy)", scribe.info(_)) {
    reconcilableFactory.artistDirectories.flatMap { artistDir =>
      val dirs = artistDir.dirs
      // "Standard" artists have albums prefixed with release year.
      if (dirs.exists(_.name.take(4).forall(_.isDigit)))
        Vector(reconcilableFactory.toArtist(artistDir))
      else // Non-standard artists, e.g., DT Sides, don't.
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
    getAlbums(artist.normalize)
      .orElse(fallback(artist.normalize))
      .getOrThrow(s"Could not find albums for artist $artist")

  private def getAlbums(artistName: ArtistName): Option[Set[Album]] =
    mf.findArtistDir(artistName).map(_.dirs.map(reconcilableFactory.toAlbum(_).get).toSet)

  private def fallback(artistName: ArtistName): Option[Set[Album]] =
    mf.albumDirs
      .filter(mf.getSongsInDir(_).head.artistName == artistName)
      .toSet
      .map((e: DirectoryRef) => reconcilableFactory.toAlbum(e).get)
      .optFilter(_.nonEmpty)
}
