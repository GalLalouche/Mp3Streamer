package models

import common.rich.path.Directory

class Artist private(val name: String, _albums: => Seq[Album]) {
  lazy val albums = _albums
}

object Artist {
  def apply(dir: Directory): Artist = new Artist(dir.name, dir.dirs.map(Album.apply))
}