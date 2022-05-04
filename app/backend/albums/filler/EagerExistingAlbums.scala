package backend.albums.filler

import backend.albums.NewAlbum
import backend.logging.Logger
import backend.recon.{Album, Artist, StringReconScorer}
import javax.inject.Singleton

import java.time.Clock

import common.rich.collections.RichTraversableOnce._

@Singleton private class EagerExistingAlbums(
    override val albums: Map[Artist, Set[Album]],
    clock: Clock,
    logger: Logger,
    stringReconScorer: StringReconScorer,
) extends ExistingAlbums {
  override def artists: Iterable[Artist] = albums.keys

  def removeExistingAndUnreleasedAlbums(
      artist: Artist, allAlbums: Seq[NewAlbum]
  ): Seq[NewAlbum] = {
    try {
      val albumTitles = albums(artist.normalized).map(_.title)
      allAlbums
          .filter(a => albumTitles.fornone(stringReconScorer(_, a.title) > 0.95))
          .filter(_.isReleased(clock))
    } catch {
      case _: NoSuchElementException =>
        logger.verbose(s"Could not find artist <$artist>")
        Nil
    }
  }
}
