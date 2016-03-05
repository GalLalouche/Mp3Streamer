package models

import common.rich.path.Directory
import common.rich.path.RichFile._
import java.io.File
import common.Debug

trait MusicFinder extends Debug {
  val dir: Directory
  val subDirs: List[String]
  val extensions: List[String]

  lazy val genreDirs = subDirs.sorted.map(x => Directory(dir / x))


  def getSongFilePaths: Seq[String] = genreDirs.par.flatMap(_.deepDirs).flatMap(getSongFilePaths(_)).toVector
  def getSongFilePaths(d: Directory): Seq[String] = d.files.filter(f => extensions.contains(f.extension)).map(_.path)
}
