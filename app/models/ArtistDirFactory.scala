package models

import com.google.inject.Inject
import mains.fixer.FixLabelsUtils

import scala.annotation.tailrec

import common.io.{DirectoryRef, PathRef}

class ArtistDirFactory @Inject() (
    af: AlbumDirFactory,
    flu: FixLabelsUtils,
) {
  def fromSong(song: Song): ArtistDir = {
    val artist = flu.validFileName(song.artistName.toLowerCase).toLowerCase
    @tailrec def go(file: PathRef): ArtistDir = file match {
      case ref: DirectoryRef if ref.name.equalsIgnoreCase(artist) =>
        ArtistDir(ref, ref.name, ref.dirs.map(af.fromDir).toSet)
      case _ => go(file.parent)
    }
    go(song.file)
  }
}
