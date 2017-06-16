package models

import common.io.DirectoryRef

import scala.collection.GenSeq

trait MusicFinder { self: MusicFinder =>
  // Why do we need this if D is already F-bounded :((
  type D <: (DirectoryRef { type D = self.D })
  def dir: D
  protected def subDirNames: Seq[String]
  def extensions: Set[String]

  def genreDirs: Seq[D] = subDirNames.sorted.map(dir.getDir(_).get)
  def albumDirs: GenSeq[D] = genreDirs
    .flatMap(_.deepDirs)
    .toVector
    .filter(_.files.exists(f => extensions.contains(f.extension)))
  def getSongFilePaths: Seq[String] = albumDirs.par.flatMap(getSongFilePathsInDir).seq
  def getSongFilePathsInDir(d: D): Seq[String] =
    d.files.filter(f => extensions.contains(f.extension)).map(_.path)
}
