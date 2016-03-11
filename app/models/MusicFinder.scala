package models

import common.Debug
import common.io.DirectoryRef
import common.rich.path.Directory
import common.rich.path.RichFile._

trait MusicFinder extends Debug {
  val dir: DirectoryRef
  val subDirs: List[String]
  val extensions: List[String]

  def genreDirs: Seq[DirectoryRef] = subDirs.sorted.map(dir.getDir(_).get)

  def getSongFilePaths: Seq[String] = genreDirs.par
    .flatMap(_.deepFiles)
    .filter(f => f.extension == "flac" || f.extension == "mp3")
    .map(_.path)
    .seq
  def getSongFilePaths(d: Directory): Seq[String] = d.files.filter(f => extensions.contains(f.extension)).map(_.path)
}
