package models

import common.Debug
import common.io.DirectoryRef

import scala.collection.GenSeq

trait MusicFinder extends Debug {
  def dir: DirectoryRef
  def subDirs: List[String]
  def extensions: List[String]

  def genreDirs: Seq[DirectoryRef] = subDirs.sorted.map(dir.getDir(_).get)
  def albumDirs: GenSeq[DirectoryRef] = genreDirs
    .flatMap(_.deepDirs)
    .toVector
    .filter(_.files.exists(f => extensions.contains(f.extension)))
  def getSongFilePaths: Seq[String] = albumDirs.flatMap(getSongFilePathsInDir).seq
  def getSongFilePathsInDir(d: DirectoryRef): Seq[String] =
    d.files.filter(f => extensions.contains(f.extension)).map(_.path)
}
