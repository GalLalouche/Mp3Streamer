package models

import common.Debug
import common.io.DirectoryRef

import scala.collection.GenSeq

trait MusicFinder extends Debug {
  val dir: DirectoryRef
  val subDirs: List[String]
  val extensions: List[String]

  def genreDirs: Seq[DirectoryRef] = subDirs.sorted.map(dir.getDir(_).get)
  def albumDirs: GenSeq[DirectoryRef] = genreDirs.par
    .flatMap(_.deepDirs)
    .filter(_.files.exists(f => extensions.contains(f.extension)))
  def getSongFilePaths: Seq[String] = albumDirs.flatMap(getSongFilePathsInDir(_)).seq
  def getSongFilePathsInDir(d: DirectoryRef): Seq[String] =
    d.files.filter(f => extensions.contains(f.extension)).map(_.path)
}
