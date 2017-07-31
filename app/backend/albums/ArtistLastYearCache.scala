package backend.albums

import java.time.LocalDate

import backend.albums.ArtistLastYearCache.Year
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon._
import common.rich.RichT._

private class ArtistLastYearCache private(lastReleaseYear: Map[Artist, Year]) {
  def artists: Iterable[Artist] = lastReleaseYear.keys

  def filterNewAlbums(artist: Artist, albums: Seq[MbAlbumMetadata]): Seq[(NewAlbum, ReconID)] = albums
      .filter(_.isOut)
      .filter(e => isLaterThanLastRelease(artist, e.releaseDate |> Year.from))
      .map(e => NewAlbum.from(artist, e) -> e.reconId)
  private def isLaterThanLastRelease(artist: Artist, y: Year) =
    lastReleaseYear(ArtistLastYearCache.canonize(artist)) < y
}

private object ArtistLastYearCache {
  private def canonize(a: Artist): Artist = Artist(a.normalize)
  case class Year(y: Int) extends AnyVal with Ordered[Year] {
    override def compare(that: Year) = y compare that.y
  }
  object Year {
    def from(ld: LocalDate) = Year(ld.getYear)
  }
  def from(albums: Seq[Album]): ArtistLastYearCache = albums
      .groupBy(a => canonize(a.artist))
      .mapValues(_.toVector.map(_.year).max |> Year.apply)
      .mapTo(new ArtistLastYearCache(_))
}
