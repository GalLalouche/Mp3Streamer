package models

import common.io.{DirectoryRef, FileRef, RefSystem}

trait MusicFinder {self =>
  type S <: RefSystem {type S = self.S}
  def dir: S#D
  def extensions: Set[String]

  /** Dirs with an extra level of sub-genre nesting, e.g., metal which has death and black metal inside it. */
  protected def genresWithSubGenres: Seq[String]
  /** Dirs which host artists/albums at the top level, e.g., musicals. */
  protected def flatGenres: Seq[String]
  private def getDir(name: String) = dir.getDir(name).get
  private def genreDirs: Seq[S#D] = (genresWithSubGenres ++ flatGenres).sorted.map(getDir)
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
      .flatMap(_.deepDirs)
      .toVector
      // Because some albums have, e.g., cover subdirectories
      .filter(_.files.exists(f => extensions.contains(f.extension)))
  def getSongFiles: Seq[S#F] = albumDirs.par.flatMap(getSongFilesInDir).seq
  def getSongFilesInDir(d: DirectoryRef): Seq[S#F] =
    d.asInstanceOf[S#D].files.filter(f => extensions.contains(f.extension))
  def parseSong(f: FileRef): Song {type F = S#F}
  def getSongsInDir(d: DirectoryRef): Seq[Song {type F = S#F}] = getSongFilesInDir(d) map parseSong
}
