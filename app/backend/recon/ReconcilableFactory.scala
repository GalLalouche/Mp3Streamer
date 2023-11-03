package backend.recon

import backend.recon.Reconcilable.SongExtractor
import javax.inject.Inject
import models.MusicFinder

import scala.io.Source
import scala.util.{Failure, Success, Try}

import common.io.{DirectoryRef, FileRef}
import common.json.ToJsonableOps.jsonifyString
import common.rich.primitives.RichOption.richOption
import common.rich.RichT._

class ReconcilableFactory @Inject() (val mf: MusicFinder) {
  type S = mf.S
  // Some artists have invalid directory characters in their name, so their directory won't match
  // the artist name. As a stupid hack, just aggregate them below.
  private val invalidDirectoryNames: Map[String, String] = Source
    .fromInputStream(getClass.getResourceAsStream("directory_renames.json"), "utf-8")
    .getLines
    .map(_.parseJsonable[Seq[String]].toVector.ensuring(_.size == 2))
    .map { case Vector(dirName, name) => (dirName, name) }
    .toMap
  def dirNameToArtist(dirName: String): Artist = Artist(
    dirName.optionOrKeep(invalidDirectoryNames.get),
  )
  // This is Try so the error could be reserved.
  def toAlbum(dir: DirectoryRef): Try[Album] =
    if (dir.name.take(4).exists(_.isDigit))
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
              artist = dirNameToArtist(dir.parent.name),
            ),
          )
        case _ => Failure(new IllegalArgumentException(s"Bad name for <$dir>"))
      }
    else
      Success(
        mf.parseSong(mf.getSongFilesInDir(dir).headOption.getOrThrow(s"Problem with $dir")).release,
      )

  def songTitle(f: FileRef): Try[String] =
    ReconcilableFactory.capture(f.name).toTry(new Exception(s"$f has invalid file name"))
  private val IgnoredFolders = Vector("Classical", "Musicals")
  def artistDirectories: Seq[S#D] = {
    val prefixLength = {
      val $ = mf.baseDir.path
      $.length + (if ($.endsWith("\\") || $.endsWith("/")) 0 else 1)
    }
    def ignore(dir: DirectoryRef): Boolean = {
      val genrePrefix = dir.path.drop(prefixLength)
      IgnoredFolders.exists(genrePrefix.startsWith)
    }
    mf.artistDirs.filterNot(ignore)
  }
  def albumDirectories: Seq[S#D] = mf.albumDirs(artistDirectories)
}

private object ReconcilableFactory {
  private val DashRegex = """\d+ - (.*)\.[^.]+""".r
  private val DotRegex = """\d+\. (.*)\.[^.]+""".r

  // TODO: Move to ScalaCommon (Composite Regex)
  private val compositeGroupMatch = Vector(DashRegex, DotRegex)
  private def capture(s: String): Option[String] =
    // TODO Move findFirst (a => Option(b))
    compositeGroupMatch
      .collectFirst(Function.unlift(_.findAllIn(s).optFilter(_.matchData.nonEmpty)))
      .map(_.group(1))
}
