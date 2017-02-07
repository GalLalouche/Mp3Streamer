package models

import common.io.DirectoryRef

import scala.collection.GenSeq

trait MusicFinder {
  def dir: DirectoryRef
  def subDirs: Seq[String]
  def extensions: Set[String]

  def genreDirs: Seq[DirectoryRef] = subDirs.sorted.map(dir.getDir(_).get)
  def albumDirs: GenSeq[DirectoryRef] = genreDirs
    .flatMap(_.deepDirs)
    .toVector
    .filter(_.files.exists(f => extensions.contains(f.extension)))
  def getSongFilePaths: Seq[String] = albumDirs.par.flatMap(getSongFilePathsInDir).seq
  def getSongFilePathsInDir(d: DirectoryRef): Seq[String] =
    d.files.filter(f => extensions.contains(f.extension)).map(_.path)
}
