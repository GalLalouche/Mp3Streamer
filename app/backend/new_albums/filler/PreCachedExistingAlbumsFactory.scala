package backend.new_albums.filler

import backend.recon.{Artist, ReconcilableFactory}
import com.google.inject.Inject
import musicfinder.ArtistDirsIndex

import common.io.DirectoryRef
import common.rich.RichT.richT
import common.rich.collections.RichMap.richMap
import common.rich.primitives.RichOption.richOption

private class PreCachedExistingAlbumsFactory @Inject() (
    artistDirsIndex: ArtistDirsIndex,
    reconcilableFactory: ReconcilableFactory,
) {
  def from(albums: Iterable[DirectoryRef]) = new PreCachedExistingAlbums(
    albums
      .map(reconcilableFactory.toAlbum(_).get)
      .groupBy(_.artist)
      .properMapValues(_.toSet),
  )

  def singleArtist(artist: Artist): PreCachedExistingAlbums = {
    val artistDir: DirectoryRef = artistDirsIndex
      .forArtist(artist)
      .getOrThrow(s"Could not find directory for artist <${artist.name}>")
    from((artistDir.dirs: Seq[DirectoryRef]).mapIf(_.isEmpty).to(Vector(artistDir)))
  }
}
