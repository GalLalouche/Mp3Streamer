package backend.albums

import monocle.macros.Lenses

import backend.recon.Artist
import backend.scorer.ModelScore

@Lenses
private case class ArtistNewAlbums(
    artist: Artist,
    artistScore: Option[ModelScore],
    albums: Seq[NewAlbum],
)
// Needed for @lenses on a private case class.
private object ArtistNewAlbums {}
