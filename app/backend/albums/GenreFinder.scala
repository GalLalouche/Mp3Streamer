package backend.albums

import backend.logging.Logger
import backend.recon.Artist
import javax.inject.{Inject, Singleton}
import models.MusicFinder

import common.Debugging
import common.rich.collections.RichTraversableOnce.richTraversableOnce

@Singleton private class GenreFinder @Inject()(mf: MusicFinder, logger: Logger) {
  private lazy val artistDirs = Debugging.timed("Fetching artistDirs") {
    mf.artistDirs
  }(logger).mapBy(_.name.toLowerCase)
  def apply(artist: Artist): Option[String] = artistDirs.get(artist.normalize).map(_.parent.name)
}
