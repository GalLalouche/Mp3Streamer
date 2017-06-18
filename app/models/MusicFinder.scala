package models

import common.io.RefSystem

import scala.collection.GenSeq

trait MusicFinder { self =>
  type S <: RefSystem {type S = self.S}
  def dir: S#D
  protected def subDirNames: Seq[String]
  def extensions: Set[String]

  def genreDirs: Seq[S#D] = subDirNames.sorted.map(dir.getDir(_).get)
  def albumDirs: GenSeq[S#D] = genreDirs
      .flatMap(_.deepDirs)
      .toVector
      .filter(_.files.exists(f => extensions.contains(f.extension)))
  def getSongFilePaths: Seq[String] = albumDirs.par.flatMap(getSongFilePathsInDir).seq
  def getSongFilePathsInDir(d: S#D): Seq[String] =
    d.files.filter(f => extensions.contains(f.extension)).map(_.path)

  def parseSong(filePath: String): Song
}
