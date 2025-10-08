package musicfinder

import backend.recon.Artist
import musicfinder.ArtistDirResult.{MultipleArtists, NoMatch, SingleArtist}

sealed trait ArtistDirResult {
  def toOption: Option[Artist] = this match {
    case SingleArtist(artist) => Some(artist)
    case MultipleArtists(_) | NoMatch => None
  }
}

object ArtistDirResult {
  case class SingleArtist(artist: Artist) extends ArtistDirResult
  case class MultipleArtists(artists: Set[Artist]) extends ArtistDirResult
  case object NoMatch extends ArtistDirResult
}
