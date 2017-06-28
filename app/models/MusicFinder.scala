package models

import common.io.{DirectoryRef, RefSystem}

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
  def getSongFiles: Seq[S#F] = albumDirs.par.flatMap(getSongFilesInDir).seq
  def getSongFilesInDir(d: DirectoryRef): Seq[S#F] =
    d.asInstanceOf[S#D].files.filter(f => extensions.contains(f.extension))
  protected def parseSong(f: S#F): Song {type F = S#F}
  def getSongsInDir(d: DirectoryRef): Seq[Song {type F = S#F}] =
    getSongFilesInDir(d) map parseSong
}
