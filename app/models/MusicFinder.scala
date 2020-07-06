package models

import common.io.{DirectoryRef, FileRef, RefSystem}

trait MusicFinder {self =>
  type S <: RefSystem {type S = self.S}
  def dir: S#D
  protected def subDirNames: Seq[String]
  def extensions: Set[String]

  private def genreDirs: Seq[S#D] = subDirNames.sorted.map(dir.getDir(_).get)
  def artistDirs: Seq[S#D] = genreDirs.view.flatMap(_.dirs).flatMap(_.dirs)
  def findArtistDir(name: String): Option[S#D] = {
    val normalizedName = name.toLowerCase
    artistDirs.find(_.name.toLowerCase == normalizedName)
  }
  def albumDirs: Seq[S#D] = genreDirs
      .flatMap(_.deepDirs)
      .toVector
      .filter(_.files.exists(f => extensions.contains(f.extension)))
  def getSongFiles: Seq[S#F] = albumDirs.par.flatMap(getSongFilesInDir).seq
  def getSongFilesInDir(d: DirectoryRef): Seq[S#F] =
    d.asInstanceOf[S#D].files.filter(f => extensions.contains(f.extension))
  def parseSong(f: FileRef): Song {type F = S#F}
  def getSongsInDir(d: DirectoryRef): Seq[Song {type F = S#F}] =
    getSongFilesInDir(d) map parseSong
}
