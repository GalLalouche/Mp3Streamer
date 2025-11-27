package backend.recon

import backend.recon.Reconcilable.SongExtractor
import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import models.{SongTitle, TrackNumber}
import musicfinder.{ArtistDirsIndex, MusicFinder, SongDirectoryParser}

import scala.collection.View
import scala.util.{Failure, Success, Try}

import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.collections.RichSeq._
import common.rich.primitives.RichOption.richOption

class ReconcilableFactory @Inject() (
    mf: MusicFinder,
    songDirectoryParser: SongDirectoryParser,
    artistDirsIndex: ArtistDirsIndex,
) {
  private type S = mf.S

  // This is Try so the error could be reserved.
  def toAlbum(dir: DirectoryRef): Try[Album] =
    if (shouldIgnore(dir))
      Failure(new IllegalArgumentException(s"'$dir' belongs to a Classical artist"))
    else if (dir.name.take(4).exists(_.isDigit))
      dir.name.split(" ", 2) match {
        case Array(yearStr, title) =>
          assert(
            // Some album years are suffixed with an ordering, e.g., 1969A.
            yearStr.length == 4 || yearStr.length == 5,
            s"<$yearStr> has weird format for <$dir>",
          )
          Success(
            Album(
              title,
              yearStr.take(4).toInt,
              artistDirsIndex.forDir(dir.parent).toOption.getOrElse(extractArtistFromAlbumDir(dir)),
            ),
          )
        case _ => Failure(new IllegalArgumentException(s"Bad name for <$dir>"))
      }
    else if (dir.name == "Singles")
      Failure(new Exception("Singles directory"))
    else
      Success(songDirectoryParser(dir).headOption.getOrThrow(s"Problem with $dir").release)

  /** Throws if artist could not be extracted! */
  def extractArtistFromAlbumDir(albumDir: DirectoryRef): Artist =
    songDirectoryParser(albumDir).head.artist

  def trySongInfo(f: FileRef): Try[(TrackNumber, SongTitle)] =
    ReconcilableFactory.capture(f.name).toTry(new Exception(s"$f has invalid file name"))
  def songInfo(f: FileRef): (TrackNumber, SongTitle) =
    trySongInfo(f).getOrElse(songDirectoryParser(f).toTuple(_.trackNumber, _.title))
  private val IgnoredFolders = Vector("Classical", "Musicals")
  private def artistDirectoriesTyped: View[S#D] = mf.artistDirs.filterNot(shouldIgnore)
  // TODO this shouldn't be here.
  def artistDirectories: View[DirectoryRef] = artistDirectoriesTyped
  private val prefixLength = {
    val $ = mf.baseDir.path
    $.length + (if ($.endsWith("\\") || $.endsWith("/")) 0 else 1)
  }
  private def shouldIgnore(dir: DirectoryRef): Boolean = {
    val genrePrefix = dir.path.drop(prefixLength)
    IgnoredFolders.exists(genrePrefix.startsWith)
  }
  def albumDirectories: View[DirectoryRef] = mf.albumDirs(artistDirectoriesTyped)
}

private object ReconcilableFactory {
  private val DashRegex = """(\d+) - (.*)\.[^.]+""".r
  private val DotRegex = """(\d+)\. (.*)\.[^.]+""".r

  private val compositeGroupMatch = Vector(DashRegex, DotRegex)
  @VisibleForTesting
  def capture(fileName: String): Option[(TrackNumber, SongTitle)] =
    compositeGroupMatch
      .firstSome(_.findAllIn(fileName).optFilter(_.matchData.nonEmpty))
      .map(_.toTuple(_.group(1).toInt, _.group(2)))
}
