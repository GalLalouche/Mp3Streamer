package backend.albums

import backend.recon.Artist
import javax.inject.{Inject, Singleton}
import models.{EnumGenre, MusicFinder}

import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.TimedLogger

@Singleton private class GenreFinder @Inject()(mf: MusicFinder, timed: TimedLogger) {
  private lazy val artistDirs = timed("Fetching artistDirs") {
    mf.artistDirs
  }.mapBy(_.name.toLowerCase)
  def apply(artist: Artist): Option[EnumGenre] =
    artistDirs.get(artist.normalize).map(mf.genre).map(EnumGenre.from)
}
