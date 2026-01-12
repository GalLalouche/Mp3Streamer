package backend.new_albums.filler

import backend.recon.{Album, Artist, ReconcilableFactory}
import com.google.inject.Inject
import musicfinder.ArtistDirsIndex

import common.io.DirectoryRef
import common.rich.RichT.richT
import common.rich.collections.RichIterator.richIterator
import common.rich.primitives.RichOption.richOption

private class PreCachedExistingAlbumsFactory @Inject() (
    artistDirsIndex: ArtistDirsIndex,
    reconcilableFactory: ReconcilableFactory,
) {
  def from(albums: Iterator[DirectoryRef]) = new PreCachedExistingAlbums(
    albums
      .flatMap(toAlbum)
      .groupBy(_.artist)
      .view
      .mapValues(_.toSet)
      .toMap,
  )

  private def toAlbum(dir: DirectoryRef): Option[Album] =
    reconcilableFactory
      .toAlbum(dir)
      .<|(_.left.foreach(e => scribe.error(s"Failed to convert <$dir> to an album due to <$e>")))
      .toOption
  def singleArtist(artist: Artist): PreCachedExistingAlbums = {
    val artistDir: DirectoryRef = artistDirsIndex
      .forArtist(artist)
      .getOrThrow(s"Could not find directory for artist <${artist.name}>")
    from((artistDir.dirs: Iterator[DirectoryRef]).mapIf(_.isEmpty).to(Iterator(artistDir)))
  }
}
