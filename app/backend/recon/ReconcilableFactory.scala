package backend.recon

import backend.recon.Reconcilable.SongExtractor
import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import models.{SongTagParser, SongTitle, TrackNumber}
import musicfinder.MusicFinder

import scala.util.{Failure, Success, Try}

import common.io.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.collections.RichSeq._
import common.rich.primitives.RichOption.richOption

class ReconcilableFactory @Inject() (val mf: MusicFinder, songTagParser: SongTagParser) {
  type S = mf.S
  def toArtist(dir: DirectoryRef): Artist = mf.dirNameToArtist(dir.name)
  // This is Try so the error could be reserved.
  def toAlbum(dir: DirectoryRef): Try[Album] =
    if (shouldIgnore(dir))
      Failure(new IllegalArgumentException(s"'$dir' belongs to a Classical artist"))
    else if (dir.name.take(4).exists(_.isDigit))
      dir.name.split(" ", 2) match {
        case Array(yearStr, title) =>
          Success(
            Album(
              title = title,
              // Some album years are suffixed with an ordering, e.g., 1969A.
              year = yearStr
                .ensuring(
                  s => s.length == 4 || s.length == 5,
                  s"<$yearStr> has weird format for <$dir>",
                )
                .take(4)
                .toInt,
              artist = toArtist(dir.parent),
            ),
          )
        case _ => Failure(new IllegalArgumentException(s"Bad name for <$dir>"))
      }
    else
      Success(
        songTagParser(mf.getSongFilesInDir(dir).headOption.getOrThrow(s"Problem with $dir")).release,
      )

  def trySongInfo(f: FileRef): Try[(TrackNumber, SongTitle)] =
    ReconcilableFactory.capture(f.name).toTry(new Exception(s"$f has invalid file name"))
  def songInfo(f: FileRef): (TrackNumber, SongTitle) =
    trySongInfo(f).getOrElse(songTagParser(f).toTuple(_.trackNumber, _.title))
  private val IgnoredFolders = Vector("Classical", "Musicals")
  def artistDirectories: Seq[S#D] = mf.artistDirs.filterNot(shouldIgnore)
  private val prefixLength = {
    val $ = mf.baseDir.path
    $.length + (if ($.endsWith("\\") || $.endsWith("/")) 0 else 1)
  }
  private def shouldIgnore(dir: DirectoryRef): Boolean = {
    val genrePrefix = dir.path.drop(prefixLength)
    IgnoredFolders.exists(genrePrefix.startsWith)
  }
  def albumDirectories: Seq[S#D] = mf.albumDirs(artistDirectories)
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
