package musicfinder

import backend.recon.Artist
import models.ArtistDir
import musicfinder.ArtistDirResult.{MultipleArtists, NoMatch, SingleArtist}

import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.Scalaz.{optionInstance, ToFunctorOps}

import common.io.{DirectoryRef, JsonableSaver}

/**
 * Unfortunately, this isn't as trivial `Artist(dir.name)`, since many artists have a directory name
 * from the actual artist name. For examples, if the artist name isn't a valid directory name in
 * Windows (e.g., R.E.M.), or when multiple artists share the same parent directory (e.g., DT Sides)
 */
private class ArtistDirsIndexImpl(
    dirToArtist: Map[DirectoryRef, Either[Artist, Seq[Artist]]],
    // This isn't a BiMap! Some dirs contain multiple artists (in which case, they won't be
    // represented in the above Map.
    artistToDir: Map[Artist, DirectoryRef],
) {
  def forDir(dir: DirectoryRef): ArtistDirResult =
    dirToArtist.get(dir).mapHeadOrElse(_.fold(SingleArtist, MultipleArtists), NoMatch)
  /** Returns `None` if there is no match. */
  def forArtist(artist: Artist): Option[DirectoryRef] = artistToDir.get(artist)
}

private object ArtistDirsIndexImpl {
  def load(saver: JsonableSaver): ArtistDirsIndexImpl = from(saver.loadArray[ArtistToDirectory])
  def from(artistDirs: Iterable[ArtistDir], saver: JsonableSaver): ArtistDirsIndexImpl = {
    val artistToDirectories = artistDirs.view.map(ArtistToDirectory.from).toVector
    saver.saveArray(artistToDirectories)
    from(artistToDirectories)
  }

  private def from(
      artistToDirectories: Seq[ArtistToDirectory],
  )(implicit di: DummyImplicit): ArtistDirsIndexImpl = {
    val dirToArtist =
      artistToDirectories
        .groupBy(_.dir)
        .mapValues(_.view.map(_.artist).toVector match {
          case Vector(e) => Left(e)
          case v => Right(v.ensuring(_.nonEmpty))
        })
    val artistToDir = dirToArtist.flatMap(e => e._2.swap.toOption.strengthR(e._1))
    new ArtistDirsIndexImpl(dirToArtist, artistToDir)
  }
}
