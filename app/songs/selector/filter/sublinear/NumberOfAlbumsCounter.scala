package songs.selector.filter.sublinear

import backend.recon.Artist
import com.google.inject.Inject

private class NumberOfAlbumsCounter @Inject() (aux: AlbumDirsAux) extends ArtistQuantifier {
  override def apply(a: Artist): Option[Int] = aux(a, _.size)
}
