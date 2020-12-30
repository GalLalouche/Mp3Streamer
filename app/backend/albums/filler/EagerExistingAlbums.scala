package backend.albums.filler

import java.time.Clock
import java.util.NoSuchElementException

import backend.albums.NewAlbum
import backend.logging.Logger
import backend.recon.{Album, Artist, StringReconScorer}
import javax.inject.Singleton
import models.MusicFinder

import common.io.DirectoryRef
import common.rich.collections.RichTraversableOnce._
import common.rich.RichT._

@Singleton private class EagerExistingAlbums private(
    override val albums: Map[Artist, Set[Album]],
    clock: Clock,
    logger: Logger,
) extends ExistingAlbums {
  override def artists: Iterable[Artist] = albums.keys

  def removeExistingAndUnreleasedAlbums(
      artist: Artist, allAlbums: Seq[NewAlbum]
  ): Seq[NewAlbum] = {
    try {
      val albumTitles = albums(artist.normalized).map(_.title)
      allAlbums
          .filter(a => albumTitles.fornone(StringReconScorer(_, a.title) > 0.95))
          .filter(_.isReleased(clock))
    } catch {
      case e: NoSuchElementException =>
        logger.warn(s"Could not find artist <$artist>", e)
        Nil
    }
  }
}

private object EagerExistingAlbums {
  def from(albums: Seq[DirectoryRef], mf: MusicFinder, clock: Clock, logger: Logger) = new EagerExistingAlbums(
    albums
        .map(ExistingAlbums.toAlbum(mf))
        .groupBy(_.artist.normalized)
        .mapValues(_.toSet)
        .view.force,
    clock,
    logger,
  )

  def singleArtist(artist: Artist, mf: MusicFinder, clock: Clock, logger: Logger): EagerExistingAlbums = {
    val artistDir = mf.findArtistDir(artist.name).get
    EagerExistingAlbums.from(
      artistDir.dirs.mapIf(_.isEmpty).to(Vector(artistDir)),
      mf,
      clock,
      logger,
    )
  }
}
