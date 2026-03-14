package backend.new_albums

import backend.recon.Artist
import backend.score.OptionalModelScore

import monocle.macros.Lenses

@Lenses
// TODO revisit visibility - widened for server test access
case class ArtistNewAlbums(
    artist: Artist,
    artistScore: OptionalModelScore,
    albums: Seq[NewAlbum],
)
object ArtistNewAlbums {}
