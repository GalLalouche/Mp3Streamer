package models

import models.MusicFinder.Genre

import common.io.{DirectoryRef, FileRef, RefSystem}
import common.rich.primitives.RichOption.richOption

trait MusicFinder {self =>
  type S <: RefSystem {type S = self.S}
  def baseDir: S#D
  def extensions: Set[String]

  /** Dirs with an extra level of sub-genre nesting, e.g., metal which has death and black metal inside it. */
  protected def genresWithSubGenres: Seq[String]
  /** Dirs which host artists/albums at the top level, e.g., musicals. */
  protected def flatGenres: Seq[String]
  private def getDir(name: String) = baseDir.getDir(name).get
  private def allGenres = genresWithSubGenres ++ flatGenres
  private def genreDirs: Seq[S#D] = allGenres.sorted.map(getDir)
  def artistDirs: Seq[S#D] = {
    def getDirs(xs: Seq[String]): Seq[S#D] = xs.view.map(getDir).flatMap(_.dirs)
    getDirs(genresWithSubGenres).flatMap(_.dirs) ++ getDirs(flatGenres)
  }
  def findArtistDir(name: String): Option[S#D] = {
    val normalizedName = name.toLowerCase
    artistDirs.find(_.name.toLowerCase == normalizedName)
  }
  def albumDirs: Seq[S#D] = albumDirs(genreDirs)
  def albumDirs(startingFrom: Seq[S#D]): Seq[S#D] = startingFrom
      .view
      .flatMap(_.deepDirs)
      // Because some albums have, e.g., cover subdirectories
      .filter(_.files.exists(f => extensions.contains(f.extension)))
      .toVector
  def getSongFiles: Seq[S#F] = albumDirs.par.flatMap(getSongFilesInDir).seq
  def getSongFilesInDir(d: DirectoryRef): Seq[S#F] =
    d.asInstanceOf[S#D].files.filter(f => extensions.contains(f.extension))
  def parseSong(f: FileRef): Song {type F = S#F}
  def getSongsInDir(d: DirectoryRef): Seq[Song {type F = S#F}] = getSongFilesInDir(d) map parseSong
  def getOptionalSongsInDir(d: DirectoryRef): Seq[OptionalSong]

  // TODO test
  def genre(dir: S#D): Genre = {
    require(dir.path startsWith this.baseDir.path, s"<$dir> is not a subdirectory of <${this.baseDir}>")
    val withoutRootPrefix = dir.path.drop(this.baseDir.path.length + 1)
    val relativeDir = dir.relativize(this.baseDir)
    if (relativeDir.isEmpty)
      return Genre.Flat(dir.name.ensuring(_ == "Musicals")) // Single album musicals, e.g., Grease
    val parentsFromBaseDir = relativeDir.reverse
    val topDirName = parentsFromBaseDir.head.name
    if (flatGenres contains topDirName)
      Genre.Flat(topDirName)
    else
      Genre.Nested(
        topDirName,
        parentsFromBaseDir.tail.headOption
            .getOrThrow(s"<$dir> is a top genre, not an actual artist directory")
            .name,
      )
  }
}

object MusicFinder {
  @deprecated("Use EnumGenre")
  sealed trait Genre extends Ordered[Genre] {
    import Genre._

    override def compare(that: Genre): Int = (this, that) match {
      case (Nested(_, _), Flat(_)) => -1
      case (Flat(_), Nested(_, _)) => 1
      case (Flat(s1), Flat(s2)) => s1 compare s2
      case (Nested(t1, s1), Nested(t2, s2)) =>
        // Reverse order by Top, because Rock comes before Metal
        implicitly[Ordering[(String, String)]].compare((t2, s1), (t1, s2))
    }
  }
  @deprecated("Use EnumGenre")
  object Genre {
    case class Flat(name: String) extends Genre // e.g., Musicals
    case class Nested(top: String, sub: String) extends Genre // e.g., Metal/Black Metal
  }
}