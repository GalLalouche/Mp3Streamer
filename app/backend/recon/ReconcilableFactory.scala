package backend.recon

import backend.new_albums.IgnoredArtists
import backend.recon.Reconcilable.SongExtractor
import backend.recon.ReconcilableFactory.AlbumParseError
import backend.recon.ReconcilableFactory.AlbumParseError.{NoSongs, SinglesDirectory, UnparsableName}
import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import models.{SongTitle, TrackNumber}
import musicfinder.{ArtistDirsIndex, MusicFinder, SongDirectoryParser}

import scala.util.Try

import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.collections.RichSeq._
import common.rich.primitives.RichOption.richOption

class ReconcilableFactory @Inject() (
    mf: MusicFinder,
    songDirectoryParser: SongDirectoryParser,
    artistDirsIndex: ArtistDirsIndex,
    directoryDiscovery: IgnoredArtists,
) {
  private type S = mf.S

  // This is Try so the error could be reserved.
  def toAlbum(dir: DirectoryRef): Either[AlbumParseError, Album] =
    if (directoryDiscovery.shouldIgnore(dir))
      Left(AlbumParseError.ClassicalArtist)
    else if (dir.name.take(4).exists(_.isDigit))
      dir.name.split(" ", 2) match {
        case Array(yearStr, title) =>
          assert(
            // Some album years are suffixed with an ordering, e.g., 1969A.
            yearStr.length == 4 || yearStr.length == 5,
            s"<$yearStr> has weird format for <$dir>",
          )
          Right(
            Album(
              title,
              yearStr.take(4).toInt,
              artistDirsIndex.forDir(dir.parent).toOption.getOrElse(extractArtistFromAlbumDir(dir)),
            ),
          )
        case _ => Left(UnparsableName)
      }
    else if (dir.name == "Singles")
      Left(SinglesDirectory)
    else
      songDirectoryParser(dir).headOption.map(_.release).toRight(NoSongs)

  /** Throws if artist could not be extracted! */
  def extractArtistFromAlbumDir(albumDir: DirectoryRef): Artist =
    songDirectoryParser(albumDir).head.artist

  def trySongInfo(f: FileRef): Try[(TrackNumber, SongTitle)] =
    ReconcilableFactory.capture(f.name).toTry(new Exception(s"$f has invalid file name"))
  def songInfo(f: FileRef): (TrackNumber, SongTitle) =
    trySongInfo(f).getOrElse(songDirectoryParser(f).toTuple(_.trackNumber, _.title))
}

object ReconcilableFactory {
  private val DashRegex = """(\d+) - (.*)\.[^.]+""".r
  private val DotRegex = """(\d+)\. (.*)\.[^.]+""".r

  private val compositeGroupMatch = Vector(DashRegex, DotRegex)
  @VisibleForTesting
  private[recon] def capture(fileName: String): Option[(TrackNumber, SongTitle)] =
    compositeGroupMatch
      .firstSome(_.findAllIn(fileName).optFilter(_.matchData.nonEmpty))
      .map(_.toTuple(_.group(1).toInt, _.group(2)))

  sealed trait AlbumParseError
  object AlbumParseError {
    case object SinglesDirectory extends AlbumParseError
    case object ClassicalArtist extends AlbumParseError
    case object NoSongs extends AlbumParseError
    case object UnparsableName extends AlbumParseError
  }
}
