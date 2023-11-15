package backend.albums

import backend.recon.Artist
import backend.scorer.OptionalModelScore

import monocle.macros.Lenses

@Lenses
private case class ArtistNewAlbums(
    artist: Artist,
    artistScore: OptionalModelScore,
    albums: Seq[NewAlbum],
)
// Needed for @lenses on a private case class.
private object ArtistNewAlbums {}
