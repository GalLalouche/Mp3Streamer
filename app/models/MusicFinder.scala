package models

import common.io.{DirectoryRef, FileRef, RefSystem}

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


  def parseSong(filePath: String): Song
  def parseSong(f: FileRef): Song = parseSong(f.path)
}
