package songs.selector

import backend.recon.Artist
import com.google.inject.Inject
import musicfinder.SongFileFinder

private class NumberOfSongsCounter @Inject() (
    aux: AlbumDirsAux,
    sff: SongFileFinder,
) extends ArtistQuantifier {
  override def apply(a: Artist): Option[Int] = aux(a, _.map(sff.getSongFilesInDir(_).size))
}
