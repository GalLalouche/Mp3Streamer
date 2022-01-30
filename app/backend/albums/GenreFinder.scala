package backend.albums

import backend.recon.Artist
import javax.inject.{Inject, Singleton}
import models.MusicFinder

import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.TimedLogger

@deprecated("Using the built-in method in MusicFinder")
@Singleton private class GenreFinder @Inject()(mf: MusicFinder, timed: TimedLogger) {
  private lazy val artistDirs = timed("Fetching artistDirs") {
    mf.artistDirs
  }.mapBy(_.name.toLowerCase)
  def apply(artist: Artist): Option[String] = artistDirs.get(artist.normalize).map(_.parent.name)
}
