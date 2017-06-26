package models

import java.io.File

import common.io.IODirectory
import common.rich.path.Directory
import common.rich.path.RichFile._

case class Album(dir: File, title: String, artistName: String, year: Int) {
  def songs(implicit mf: MusicFinder): Seq[Song] = mf.getSongsInDir(new IODirectory(dir))
}

object Album {
  def apply(dir: Directory) = new Album(dir,
      dir.name.replaceAll("\\d+\\w? ?", ""),
      dir.parent.name,
      dir.files.iterator.filter(x => List("mp3", "flac").contains(x.extension)).map(Song(_).year).next)
}
