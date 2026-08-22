package songs.selector

import backend.recon.Artist

private trait ValueCounter {
  /** Must return a positive value. */
  def apply(a: Artist): Option[Int]
}
