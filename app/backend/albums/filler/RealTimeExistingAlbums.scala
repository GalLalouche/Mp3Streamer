package backend.albums.filler

import javax.inject.Inject

import backend.logging.LoggingLevel
import backend.recon.{Album, Artist, ReconcilableFactory}
import models.MusicFinder

import common.io.JsonMapFile
import common.rich.primitives.RichOption.richOption
import common.TimedLogger

private class RealTimeExistingAlbums @Inject() (
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

  override def albums: Artist => Set[Album] = artist => {
    val normalizedName = artist.normalize
    getAlbums(normalizedName)
      .orElse(
        misplacedArtists
          .get(normalizedName)
          .flatMap(getAlbums)
          .map(_.filter(_.artist.normalize == normalizedName)),
      )
      .getOrThrow(s"Could not find albums for artist $artist")
  }

  private def getAlbums(artistName: String): Option[Set[Album]] =
    mf.findArtistDir(artistName).map(_.dirs.map(reconcilableFactory.toAlbum(_).get).toSet)

  // Some artists have misplaced directories, e.g., The Neal Morse Band might be under the
  // Neal Morse folder. As a stupid hack, we just aggregate them in a single file.
  private def misplacedArtists: Map[String, String] =
    JsonMapFile.readJsonMap(getClass.getResourceAsStream("directory_renames.json"))
}
