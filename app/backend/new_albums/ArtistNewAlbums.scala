package backend.new_albums

import backend.recon.Artist
import backend.score.OptionalModelScore

import monocle.macros.Lenses

@Lenses
private case class ArtistNewAlbums(
    artist: Artist,
    artistScore: OptionalModelScore,
    albums: Seq[NewAlbum],
)
// Needed for @lenses on a private case class.
private object ArtistNewAlbums {}
