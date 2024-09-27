package backend.albums.filler

import java.time.Clock
import javax.inject.Inject

import backend.albums.NewAlbum
import backend.recon.{Artist, StringReconScorer}

import common.rich.collections.RichTraversableOnce.richTraversableOnce

private class FilterExistingAlbums @Inject() (
    ea: ExistingAlbums,
    clock: Clock,
    stringReconScorer: StringReconScorer,
) {
  def apply(artist: Artist, allAlbums: Seq[NewAlbum]): Seq[NewAlbum] =
    try {
      val albumTitles = ea.albums(artist.normalized).map(_.title)
      allAlbums
        .filter(a => albumTitles.fornone(stringReconScorer(_, a.title) > 0.95))
        .filter(_.isReleased(clock))
    } catch {
      case e: NoSuchElementException =>
        scribe.warn(s"Could not find artist <$artist>", e)
        Nil
    }
}
