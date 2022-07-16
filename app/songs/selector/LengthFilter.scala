package songs.selector

import backend.recon.Reconcilable.SongExtractor
import models.{Genre, GenreFinder, Song}

import scala.concurrent.duration.Duration

import common.Filter

private class LengthFilter(genreFinder: GenreFinder, minLength: Duration) extends Filter[Song] {
  override def passes(a: Song): Boolean = genreFinder.forArtist(a.artist) match {
    case Some(Genre.Metal(_)) => a.duration >= minLength
    case _ => true
  }
}
