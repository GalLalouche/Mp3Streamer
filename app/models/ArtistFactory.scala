package models

import javax.inject.Inject

import mains.fixer.FixLabelsUtils

import scala.annotation.tailrec

import common.io.{DirectoryRef, PathRef}

class ArtistFactory @Inject() (
    af: AlbumFactory,
    flu: FixLabelsUtils,
) {
  def fromDir(dir: DirectoryRef): Artist = Artist(dir.name, dir.dirs.map(af.fromDir).toSet)
  def fromSong(song: Song): Artist = {
    val artist = flu.validFileName(song.artistName.toLowerCase).toLowerCase
    @tailrec def go(file: PathRef): Artist = file match {
      case ref: DirectoryRef if ref.name.toLowerCase == artist => fromDir(ref)
      case _ => go(file.parent)
    }
    go(song.file)
  }
}
