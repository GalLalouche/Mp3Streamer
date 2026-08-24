package songs.selector

import backend.recon.Artist

private trait ArtistQuantifier {
  /** Must return a positive value. */
  def apply(a: Artist): Option[Int]
}
