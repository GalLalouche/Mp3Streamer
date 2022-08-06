package backend.albums

import backend.recon.Artist
import backend.scorer.ModelScore

import monocle.macros.Lenses

@Lenses
private case class ArtistNewAlbums(artist: Artist, artistScore: Option[ModelScore], albums: Seq[NewAlbum])
// Needed for @lenses on a private case class.
private object ArtistNewAlbums {}