package models

import common.Debug
import common.io.DirectoryRef

trait MusicFinder extends Debug {
  val dir: DirectoryRef
  val subDirs: List[String]
  val extensions: List[String]

  def genreDirs: Seq[DirectoryRef] = subDirs.sorted.map(dir.getDir(_).get)

  def getSongFilePaths: Seq[String] = genreDirs.toStream.par
    .flatMap(_.deepDirs)
    .map(getSongFilePathsInDir(_))
    .filter(_.nonEmpty)
    .flatten
    .seq
  def getSongFilePathsInDir(d: DirectoryRef): Seq[String] = d.files.filter(f => extensions.contains(f.extension)).map(_.path)
}
