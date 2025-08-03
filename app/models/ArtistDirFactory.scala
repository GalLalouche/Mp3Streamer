package models

import backend.recon.Reconcilable.SongExtractor
import com.google.inject.Inject
import mains.fixer.FixLabelsUtils
import musicfinder.ArtistDirsIndex

import scala.annotation.tailrec

import common.io.{DirectoryRef, PathRef}
import common.rich.RichT.richT
import common.rich.primitives.RichOption.richOption

class ArtistDirFactory @Inject() (
    af: AlbumDirFactory,
    flu: FixLabelsUtils,
    artistDirsIndex: ArtistDirsIndex,
) {
  def fromSong(song: Song): ArtistDir = {
    val artist = flu.validFileName(song.artistName.toLowerCase).toLowerCase
    @tailrec def go(file: PathRef): DirectoryRef = file match {
      case ref: DirectoryRef if ref.name.equalsIgnoreCase(artist) => ref
      case _ =>
        if (file.hasParent) go(file.parent)
        else artistDirsIndex.forArtist(song.artist).getOrThrow(s"No artist found for <$song>")
    }

    val dir = go(song.file)

    ArtistDir(
      dir,
      dir.name,
      // If dirs is empty, this a single album artist, e.g., greatest hits.
      dir.dirs.map(af.fromDir).toSet.mapIf(_.isEmpty).to(Set(af.fromDir(dir))),
    )
  }
}
