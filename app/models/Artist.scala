package models

import common.rich.path.Directory

class Artist(val dir: Directory, val name: String) {
  lazy val albums = dir.dirs.map(Album.apply)
}

object Artist {
  def apply(dir: Directory): Artist = new Artist(dir, dir.name)
}