package models

import common.io.DirectoryRef

// TODO make songs a class field, and transform Song.album to require an implicit MF
case class Album(dir: DirectoryRef, title: String, artistName: String, year: Int) {
  def songs(implicit mf: MusicFinder): Seq[Song] = mf.getSongsInDir(dir)
}

object Album {
  def apply(dir: DirectoryRef)(implicit mf: MusicFinder) = new Album(dir,
    dir.name.replaceAll("\\d+\\w? ?", ""),
    dir.parent.name,
    mf.getSongsInDir(dir).head.year)
}
