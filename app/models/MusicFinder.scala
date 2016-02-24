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

  def getSongFilePaths: IndexedSeq[String] = {
    (genreDirs.flatMap(_.files) ++ (genreDirs.flatMap(_.dirs).par.flatMap(_.deepFiles)))
      .filter(x => extensions.contains(x.extension))
      .map(_.path)
      .toVector
  }
}
