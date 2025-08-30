package musicfinder

import backend.recon.Artist
import genre.GenreFinder
import models.ArtistDir
import musicfinder.ArtistDirResult.{MultipleArtists, NoMatch, SingleArtist}

import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.Scalaz.optionInstance

import common.io.{DirectoryRef, JsonableSaver}
import common.json.Jsonable
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichBoolean.richBoolean

/**
 * Unfortunately, this isn't as trivial `Artist(dir.name)`, since many artists have a directory name
 * from the actual artist name. For examples, if the artist name isn't a valid directory name in
 * Windows (e.g., R.E.M.), or when multiple artists share the same parent directory (e.g., DT Sides)
 */
private class ArtistDirsIndexImpl(
    dirToArtist: Map[DirectoryRef, Either[Artist, Seq[Artist]]],
    // This isn't a BiMap! Some dirs contain multiple artists (in which case, they won't be
    // represented in the above Map).
    artistToDir: Map[Artist, DirectoryRef],
) {
  def forDir(dir: DirectoryRef): ArtistDirResult =
    dirToArtist.get(dir).mapHeadOrElse(_.fold(SingleArtist, MultipleArtists), NoMatch)
  /** Returns `None` if there is no match. */
  def forArtist(artist: Artist): Option[DirectoryRef] = artistToDir.get(artist)
}

private object ArtistDirsIndexImpl {
  def load(saver: JsonableSaver)(implicit json: Jsonable[ArtistToDirectory]): ArtistDirsIndexImpl =
    from(saver.loadArray[ArtistToDirectory])
  def from(
      artistDirs: Iterable[ArtistDir],
      saver: JsonableSaver,
      genreFinder: GenreFinder,
  )(implicit json: Jsonable[ArtistToDirectory]): ArtistDirsIndexImpl = {
    val artistToDirectories =
      artistDirs.view
        .map(ArtistToDirectory.from)
        .groupBy(_.artist)
        .map { case (a, dirs) =>
          val dir = dirs.view.map(_.dir).toVector.distinct match {
            case Vector(dir) => dir
            case dirs =>
              if (dirs.existsNot(genreFinder.isClassical))
                // Classical tracks are often sorted by composer, not (performing) artist.
                scribe.debug(s"Multiple dirs found for artist <$a>: $dirs")
              // Having multiple dirs for a single artists can stem from them being listed in a
              // split in another artist's directory, or song files appearing at the top level.
              // So we just take the biggest directory in this case.
              dirs.minBy(_.deepFiles.size)
          }
          ArtistToDirectory(a, dir)
        }
        .toVector

    saver.saveArray(artistToDirectories)
    from(artistToDirectories)
  }

  private def from(artistToDirectories: Seq[ArtistToDirectory])(implicit di: DummyImplicit) =
    new ArtistDirsIndexImpl(
      dirToArtist = artistToDirectories
        .groupBy(_.dir)
        .map { case (k, v) =>
          k -> (v.view.map(_.artist).toVector match {
            case Vector(e) => Left(e)
            case v =>
              if (v.allUnique.isFalse)
                scribe.warn(s"Multiple repeating artists found for directory <$k>")
              Right(v.ensuring(_.nonEmpty))
          })
        },
      // TuplePLenses requires explicit types here for some reason :\
      artistToDir = artistToDirectories.groupBy(_.artist).map(e => e._1 :-> toSingle(e._2)),
    )

  private def toSingle(xs: Seq[ArtistToDirectory])(artist: Artist): DirectoryRef =
    xs.view.map(_.dir).toVector match {
      case Vector(x) => x
      case v =>
        throw new IllegalArgumentException(
          s"Expected a single directory for <$artist>, but found <$v>",
        )
    }
}
